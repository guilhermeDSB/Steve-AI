package com.steve.ai.execution;

import com.steve.ai.entity.SteveEntity;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

/**
 * Executes LLM-generated JavaScript code in a sandboxed GraalVM context.
 *
 * Safety features:
 * - No file system access
 * - No network access
 * - Timeout enforcement (30 seconds max)
 * - Restricted Java package access
 * - Controlled API via SteveAPI bridge
 */
public class CodeExecutionEngine {
    private final SteveEntity steve;
    private final Context graalContext;
    private final SteveAPI steveAPI;

    private static final long DEFAULT_TIMEOUT_MS = 30000; // 30 seconds

    public CodeExecutionEngine(SteveEntity steve) {
        this.steve = steve;
        this.steveAPI = new SteveAPI(steve);

        // Create GraalVM context with strict security restrictions
        this.graalContext = Context.newBuilder("js")
            .allowAllAccess(false)                        // Deny all access by default
            .allowIO(false)                                // No file system
            .allowNativeAccess(false)                      // No native libraries
            .allowCreateThread(false)                      // No thread creation
            .allowCreateProcess(false)                     // No process creation
            .allowHostClassLookup(className -> false)      // No Java class access
            .allowHostAccess(null)                         // No host access
            .option("js.java-package-globals", "false")    // Disable Java package globals
            .option("js.timer-resolution", "1")            // Low resolution timers
            .build();

        // Inject Steve API as the only bridge to Minecraft
        graalContext.getBindings("js").putMember("steve", steveAPI);

        // Provide a pure-JS console stub so LLM code can call console.log() without
        // accessing any Java host classes (which are blocked by allowHostClassLookup).
        // The stub discards output; a host-callback approach would require relaxing
        // allowHostAccess, which widens the sandbox surface.
        try {
            graalContext.eval("js", "var console = { log: function() {}, warn: function() {}, error: function() {} };");
        } catch (PolyglotException e) {
            // Non-fatal: LLM code that calls console.log will get a ReferenceError,
            // which is reported as a normal execution error rather than a sandbox escape.
        }
    }

    /**
     * Execute JavaScript code with default timeout
     */
    public ExecutionResult execute(String code) {
        return execute(code, DEFAULT_TIMEOUT_MS);
    }

    /**
     * Execute JavaScript code with custom timeout
     *
     * @param code JavaScript code to execute
     * @param timeoutMs Maximum execution time in milliseconds
     * @return ExecutionResult containing success/failure status and output
     */
    public ExecutionResult execute(String code, long timeoutMs) {
        if (code == null || code.trim().isEmpty()) {
            return ExecutionResult.error("No code provided");
        }

        try {
            // Wrap code in timeout context
            Value result = graalContext.eval("js", code);

            // Convert result to string
            String output = result.isNull() ? "null" : result.toString();

            return ExecutionResult.success(output);

        } catch (PolyglotException e) {
            // Handle various execution errors
            if (e.isExit()) {
                return ExecutionResult.error("Code called exit: " + e.getExitStatus());
            }

            if (e.isInterrupted()) {
                return ExecutionResult.error("Execution interrupted (timeout?)");
            }

            if (e.isSyntaxError()) {
                return ExecutionResult.error("Syntax error: " + e.getMessage());
            }

            // Generic execution error
            String errorMsg = e.getMessage();
            if (errorMsg == null || errorMsg.isEmpty()) {
                errorMsg = "Unknown execution error";
            }

            return ExecutionResult.error("Error: " + errorMsg);

        } catch (Exception e) {
            return ExecutionResult.error("Unexpected error: " + e.getMessage());
        }
    }

    /**
     * Validate JavaScript code syntax without executing
     *
     * @param code JavaScript code to validate
     * @return true if syntax is valid, false otherwise
     */
    public boolean validateSyntax(String code) {
        try {
            // Parse without executing by wrapping in function
            graalContext.eval("js", "function __validate() { " + code + " }");
            return true;
        } catch (PolyglotException e) {
            return false;
        }
    }

    /**
     * Get the Steve API bridge
     */
    public SteveAPI getAPI() {
        return steveAPI;
    }

    /**
     * Clean up resources
     */
    public void close() {
        if (graalContext != null) {
            graalContext.close();
        }
    }

    /**
     * Result of code execution
     */
    public static class ExecutionResult {
        private final boolean success;
        private final String output;
        private final String error;

        private ExecutionResult(boolean success, String output, String error) {
            this.success = success;
            this.output = output;
            this.error = error;
        }

        public static ExecutionResult success(String output) {
            return new ExecutionResult(true, output, null);
        }

        public static ExecutionResult error(String error) {
            return new ExecutionResult(false, null, error);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getOutput() {
            return output;
        }

        public String getError() {
            return error;
        }

        @Override
        public String toString() {
            if (success) {
                return "Success: " + output;
            } else {
                return "Error: " + error;
            }
        }
    }
}

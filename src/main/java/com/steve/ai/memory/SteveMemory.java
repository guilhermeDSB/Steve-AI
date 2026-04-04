package com.steve.ai.memory;

import com.steve.ai.entity.SteveEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SteveMemory {
    private final SteveEntity steve;
    private String currentGoal;
    private final LinkedList<String> recentActions;
    private static final int MAX_RECENT_ACTIONS = 20;

    public SteveMemory(SteveEntity steve) {
        this.steve = steve;
        this.currentGoal = "";
        this.recentActions = new LinkedList<>();
    }

    public String getCurrentGoal() {
        return currentGoal;
    }

    public void setCurrentGoal(String goal) {
        this.currentGoal = goal;
    }

    public void addAction(String action) {
        recentActions.addLast(action);
        if (recentActions.size() > MAX_RECENT_ACTIONS) {
            recentActions.removeFirst();
        }
    }

    public List<String> getRecentActions(int count) {
        int size = Math.min(count, recentActions.size());
        List<String> result = new ArrayList<>();
        
        int startIndex = Math.max(0, recentActions.size() - count);
        for (int i = startIndex; i < recentActions.size(); i++) {
            result.add(recentActions.get(i));
        }
        
        return result;
    }

    /**
     * Clears the current goal stored in memory.
     *
     * <p>The actual task queue lives in {@link com.steve.ai.action.ActionExecutor}.
     * This method is kept for backward compatibility with callers such as
     * {@code SteveCommands.stopSteve()} that call both
     * {@code stopCurrentAction()} and this method together.</p>
     *
     * @deprecated Use {@code ActionExecutor.stopCurrentAction()} to fully stop
     *             execution; this method only resets the remembered goal string.
     */
    @Deprecated
    public void clearTaskQueue() {
        currentGoal = "";
    }

    public void saveToNBT(CompoundTag tag) {
        tag.putString("CurrentGoal", currentGoal);
        
        ListTag actionsList = new ListTag();
        for (String action : recentActions) {
            actionsList.add(StringTag.valueOf(action));
        }
        tag.put("RecentActions", actionsList);
    }

    public void loadFromNBT(CompoundTag tag) {
        if (tag.contains("CurrentGoal")) {
            currentGoal = tag.getString("CurrentGoal");
        }
        
        if (tag.contains("RecentActions")) {
            recentActions.clear();
            ListTag actionsList = tag.getList("RecentActions", 8); // 8 = String type
            for (int i = 0; i < actionsList.size(); i++) {
                recentActions.add(actionsList.getString(i));
            }
        }
    }
}


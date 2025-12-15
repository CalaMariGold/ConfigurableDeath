package com.calamarigold.configurabledeath.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a damage source rule for inventory part behavior
 * Format: "damageSource:part:action"
 */
public class DamageSourceRule {
    public final String damageSource;
    public final String part;
    public final boolean shouldDrop; // true = drop, false = keep
    
    public DamageSourceRule(String damageSource, String part, boolean shouldDrop) {
        this.damageSource = damageSource;
        this.part = part;
        this.shouldDrop = shouldDrop;
    }
    
    /**
     * Parse a list of rule strings into a map for quick lookup
     * Map structure: damageSource -> part -> shouldDrop
     */
    public static Map<String, Map<String, Boolean>> parseRules(List<? extends String> ruleStrings) {
        Map<String, Map<String, Boolean>> rules = new HashMap<>();
        
        if (ruleStrings == null) {
            return rules;
        }
        
        for (String ruleString : ruleStrings) {
            if (ruleString == null || ruleString.trim().isEmpty()) {
                continue;
            }
            
            String[] parts = ruleString.split(":");
            if (parts.length != 3) {
                ModLogger.warn("Invalid damage source rule format: '{}'. Expected format: 'damageSource:part:action'", ruleString);
                continue;
            }
            
            String damageSource = parts[0].trim();
            String part = parts[1].trim();
            String action = parts[2].trim().toLowerCase();
            
            if (damageSource.isEmpty() || part.isEmpty() || action.isEmpty()) {
                ModLogger.warn("Invalid damage source rule: '{}'. All parts must be non-empty.", ruleString);
                continue;
            }
            
            boolean shouldDrop;
            if ("drop".equals(action)) {
                shouldDrop = true;
            } else if ("keep".equals(action)) {
                shouldDrop = false;
            } else {
                ModLogger.warn("Invalid action in rule '{}': '{}'. Must be 'drop' or 'keep'.", ruleString, action);
                continue;
            }
            
            // Validate part name
            if (!isValidPart(part)) {
                ModLogger.warn("Invalid part in rule '{}': '{}'. Valid parts: hotbar, armor, mainhand, offhand, mainInventory, inventory", ruleString, part);
                continue;
            }
            
            // Store rule: damageSource -> part -> shouldDrop
            rules.computeIfAbsent(damageSource, k -> new HashMap<>()).put(part, shouldDrop);
        }
        
        return rules;
    }
    
    private static boolean isValidPart(String part) {
        return "hotbar".equals(part) || 
               "armor".equals(part) || 
               "mainhand".equals(part) || 
               "offhand".equals(part) || 
               "mainInventory".equals(part) || 
               "inventory".equals(part);
    }
}


package com.example.javaqua.atlas;

import com.example.javaqua.model.Aquarium;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SuggestionEngine {

    public enum Rating {
        GREEN, YELLOW, RED, UNKNOWN
    }

    public static class CompatibilityResult {
        public Rating overallRating = Rating.GREEN;
        public List<String> messages = new ArrayList<>();
    }

    public static Rating calculateRating(FishPreview fish, Aquarium aquarium) {
        if (aquarium == null) return Rating.UNKNOWN;
        if (aquarium.getVolumeLiters() > 0 && aquarium.getVolumeLiters() < fish.getMinVolume()) return Rating.RED;

        double temp = aquarium.getTemperature();
        if (temp > 0 && fish.getMinTemp() > 0 && fish.getMaxTemp() > 0) {
            if (temp < fish.getMinTemp() - 2 || temp > fish.getMaxTemp() + 2) return Rating.RED;
            if (temp < fish.getMinTemp() || temp > fish.getMaxTemp()) return Rating.YELLOW;
        }
        return Rating.GREEN;
    }

    public static CompatibilityResult getCompatibilityReport(Aquarium aquarium, List<FishPreview> uniqueSpecies, Map<String, Integer> counts) {
        CompatibilityResult report = new CompatibilityResult();
        if (aquarium == null) return report;
        
        if (uniqueSpecies == null || uniqueSpecies.isEmpty() || counts == null || counts.isEmpty()) {
            report.messages.add("No inhabitants to analyze.");
            report.overallRating = Rating.UNKNOWN;
            return report;
        }

        double totalLoad = 0;
        int totalFishCount = 0;
        int aggressiveCount = 0;
        int speciesCount = uniqueSpecies.size();

        for (FishPreview fish : uniqueSpecies) {
            int count = counts.getOrDefault(fish.getId(), 0);
            if (count <= 0) continue;
            totalFishCount += count;

            // 1. Environmental Suitability
            Rating r = calculateRating(fish, aquarium);
            if (r == Rating.RED) {
                report.overallRating = Rating.RED;
                report.messages.add("Incompatible: Conditions unsuitable for " + fish.getName());
            } else if (r == Rating.YELLOW && report.overallRating == Rating.GREEN) {
                report.overallRating = Rating.YELLOW;
                report.messages.add("Warning: Temperature is sub-optimal for " + fish.getName());
            }

            // 2. Schooling (Inferred from descriptions if field missing, but let's use 5 as a safe default for common schooling fish)
            String desc = (fish.getDescription() + fish.getShortDescription()).toLowerCase();
            boolean isSchooling = desc.contains("schooling") || desc.contains("shoaling") || desc.contains("social");
            if (isSchooling && count < 5) {
                if (report.overallRating == Rating.GREEN) report.overallRating = Rating.YELLOW;
                report.messages.add("Schooling: " + fish.getName() + " is a social species (prefer groups of 5+).");
            }

            // 3. Aggression
            if (desc.contains("aggressive") || desc.contains("territorial") || desc.contains("predator")) {
                aggressiveCount++;
                if (speciesCount > 1) {
                    if (report.overallRating == Rating.GREEN) report.overallRating = Rating.YELLOW;
                    report.messages.add("Aggression: " + fish.getName() + " may harass tank mates.");
                }
            }

            // 4. Bio-load estimation
            // Using minVolume as a scale for bioload: 1 unit per 10L of minimum required space
            double fishLoad = Math.max(1.0, fish.getMinVolume() / 10.0);
            totalLoad += fishLoad * count;
        }

        // 5. Overcrowding check
        if (aquarium.getVolumeLiters() > 0) {
            if (totalLoad > aquarium.getVolumeLiters()) {
                report.overallRating = Rating.RED;
                report.messages.add("Overcrowded: Bio-load exceeds tank capacity (" + (int)totalLoad + " units used).");
            } else if (totalLoad > aquarium.getVolumeLiters() * 0.8) {
                if (report.overallRating == Rating.GREEN) report.overallRating = Rating.YELLOW;
                report.messages.add("Warning: Tank is reaching maximum bio-load capacity.");
            }
        }

        if (report.messages.isEmpty()) {
            report.messages.add("Health check: All inhabitants look perfectly fine!");
        }

        return report;
    }
}
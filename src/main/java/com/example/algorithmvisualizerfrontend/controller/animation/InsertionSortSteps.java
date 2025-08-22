package com.example.algorithmvisualizerfrontend.controller.animation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class InsertionSortSteps {
    public List<Step> generate(List<Integer> input) {
        List<Integer> a = new ArrayList<>(input);
        List<Step> steps = new ArrayList<>();
        int n = a.size();

        for (int i = 1; i < n; i++) {
            steps.add(Step.highlight(List.of(i), Step.Role.ACTIVE, 200));
            int j = i;
            while (j > 0) {
                steps.add(Step.compare(j - 1, j, 300));
                if (a.get(j - 1) > a.get(j)) {
                    Collections.swap(a, j - 1, j);
                    steps.add(Step.swap(j - 1, j, 400));
                    steps.add(Step.clear(120));
                    j--;
                } else {
                    steps.add(Step.clear(120));
                    break;
                }
            }
            steps.add(Step.markSorted(range(0, i + 1), 150));
        }

        steps.add(Step.markSorted(range(0, n), 0));
        steps.add(Step.done(0));
        return steps;
    }

    private List<Integer> range(int fromInc, int toExcl) {
        List<Integer> out = new ArrayList<>();
        for (int k = fromInc; k < toExcl; k++) out.add(k);
        return out;
    }
}
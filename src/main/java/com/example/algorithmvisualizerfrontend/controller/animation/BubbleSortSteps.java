package com.example.algorithmvisualizerfrontend.controller.animation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BubbleSortSteps {
    public List<Step> generate(List<Integer> input) {
        List<Integer> a = new ArrayList<>(input);
        List<Step> steps = new ArrayList<>();
        int n = a.size();
        for (int pass = 0; pass < n - 1; pass++) {
            for (int i = 0; i < n - 1 - pass; i++) {
                steps.add(Step.compare(i, i + 1, 250));
                if (a.get(i) > a.get(i + 1)) {
                    Collections.swap(a, i, i + 1);
                    steps.add(Step.swap(i, i + 1, 300));
                }
                steps.add(Step.clear(120));
            }
            steps.add(Step.markSorted(List.of(n - 1 - pass), 200));
        }
        steps.add(Step.markSorted(range(0, n), 0));
        steps.add(Step.done(0));
        return steps;
    }

    private List<Integer> range(int fromInc, int toExcl) {
        List<Integer> out = new ArrayList<>();
        for (int i = fromInc; i < toExcl; i++) out.add(i);
        return out;
    }
}
package com.example.algorithmvisualizerfrontend.controller;

import com.example.algorithmvisualizerfrontend.controller.animation.BubbleSortSteps;
import com.example.algorithmvisualizerfrontend.controller.animation.InsertionSortSteps;
import com.example.algorithmvisualizerfrontend.controller.animation.Step;
import com.example.algorithmvisualizerfrontend.model.AlgorithmDto;
import javafx.animation.*;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class AnimationController {
    @FXML private Button startButton;
    @FXML private Button pauseButton;
    @FXML private Button unpauseButton;
    @FXML private Button resetButton;
    @FXML private Slider speedSlider;
    @FXML private Label speedValueLabel;
    @FXML private HBox barsBox;
    @FXML private Rectangle legendDefaultRect;
    @FXML private Rectangle legendActiveRect;
    @FXML private Rectangle legendSwapRect;
    @FXML private Rectangle legendSortedRect;

    private AlgorithmDto algorithm;
    private final List<Integer> originalData = new ArrayList<>();
    private final List<Integer> data = new ArrayList<>();
    private final List<StackPane> barNodes = new ArrayList<>();
    private Timeline timeline;
    private ParallelTransition currentSwap;
    private boolean pausedByUser = false;
    private boolean finished = false;
    private List<Step> steps = List.of();
    private double barWidth = 36;
    private double barMaxHeight = 240;

    private final Color cDefault = Color.web("#5B8DEF");
    private final Color cActive = Color.web("#F2C94C");
    private final Color cSwap = Color.web("#F76E6E");
    private final Color cSorted = Color.web("#27AE60");

    public void setup(AlgorithmDto algorithm, List<Integer> numbers) {
        this.algorithm = algorithm;
        originalData.clear();
        originalData.addAll(numbers);
        data.clear();
        data.addAll(numbers);
        initLegendColors();
        renderBars();
        prepareSteps();

        speedValueLabel.textProperty().bind(Bindings.createStringBinding(
                () -> String.format("x%.2f", speedSlider.getValue()),
                speedSlider.valueProperty()
        ));

        setButtonsIdle();
    }

    @FXML
    private void onStart() {
        if (finished) return;
        if (timeline != null && timeline.getStatus() != Timeline.Status.STOPPED) return;
        setButtonsRunning();
        runTimeline();
    }

    @FXML
    private void onPause() {
        if (timeline == null) return;
        pausedByUser = true;
        timeline.pause();
        if (currentSwap != null) currentSwap.pause();
        setButtonsPaused();
    }

    @FXML
    private void onUnpause() {
        if (timeline == null || finished) return;
        pausedByUser = false;
        if (currentSwap != null) currentSwap.play();
        timeline.play();
        setButtonsRunning();
    }

    @FXML
    private void onReset() {
        if (timeline != null) timeline.stop();
        if (currentSwap != null) { currentSwap.stop(); currentSwap = null; }
        pausedByUser = false;
        finished = false;
        data.clear();
        data.addAll(originalData);
        renderBars();
        prepareSteps();
        setButtonsIdle();
    }

    private void initLegendColors() {
        legendDefaultRect.setFill(cDefault);
        legendActiveRect.setFill(cActive);
        legendSwapRect.setFill(cSwap);
        legendSortedRect.setFill(cSorted);
    }

    private void prepareSteps() {
        String name = algorithm.getName() == null ? "" : algorithm.getName().trim().toLowerCase();
        if (name.contains("bubble")) {
            steps = new BubbleSortSteps().generate(data);
        } else if(name.contains("insertion")){
            steps = new InsertionSortSteps().generate(data);
        }
    }

    private void runTimeline() {
        if (timeline != null) timeline.stop();
        timeline = new Timeline();

        timeline.setRate(speedSlider.getValue());
        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (timeline != null) timeline.setRate(newVal.doubleValue());
        });

        int t = 0;
        for (Step s : steps) {
            int dur = Math.max(1, s.durationMs == 0 ? 600 : s.durationMs);
            t += dur;
            Step stepRef = s;
            timeline.getKeyFrames().add(new KeyFrame(Duration.millis(t), e -> applyStep(stepRef)));
        }

        timeline.setOnFinished(e -> setButtonsFinished());
        timeline.playFromStart();
    }

    private void applyStep(Step s) {
        switch (s.type) {
            case HIGHLIGHT -> setRole(s.indices, s.role);
            case COMPARE -> setRole(List.of(s.i, s.j), Step.Role.ACTIVE);
            case SWAP -> animateSwapAndPauseTimeline(s.i, s.j, Math.max(300, s.durationMs));
            case MARK_SORTED -> setRole(s.indices, Step.Role.SORTED);
            case CLEAR -> clearRoles();
            case DONE -> {
                clearRoles();
                setButtonsFinished();
            }
        }
    }

    private void renderBars() {
        barsBox.getChildren().clear();
        barNodes.clear();
        int max = data.stream().mapToInt(Integer::intValue).max().orElse(1);
        for (int v : data) {
            double h = Math.max(12, (v / (double) max) * barMaxHeight);
            Rectangle rect = new Rectangle(barWidth, h);
            rect.setArcWidth(8);
            rect.setArcHeight(8);
            rect.setFill(cDefault);
            Label label = new Label(Integer.toString(v));
            StackPane sp = new StackPane(rect, label);
            sp.setAlignment(Pos.BOTTOM_CENTER);
            barNodes.add(sp);
            barsBox.getChildren().add(sp);
        }
    }

    private void setRole(List<Integer> idx, Step.Role role) {
        for (int i = 0; i < barNodes.size(); i++) {
            Rectangle r = (Rectangle) barNodes.get(i).getChildren().get(0);
            if (idx != null && idx.contains(i)) {
                r.setFill(switch (role) {
                    case ACTIVE -> cActive;
                    case SWAP -> cSwap;
                    case SORTED -> cSorted;
                    default -> cDefault;
                });
            } else if (role != Step.Role.SORTED) {
                if (!r.getFill().equals(cSorted)) r.setFill(cDefault);
            }
        }
    }

    private void clearRoles() {
        for (StackPane sp : barNodes) {
            Rectangle r = (Rectangle) sp.getChildren().get(0);
            if (!r.getFill().equals(cSorted)) r.setFill(cDefault);
        }
    }

    private void animateSwapAndPauseTimeline(int i, int j, int ms) {
        if (i < 0 || j < 0 || i >= barNodes.size() || j >= barNodes.size()) return;

        boolean wasRunning = timeline != null && timeline.getStatus() == Timeline.Status.RUNNING;
        if (timeline != null) timeline.pause();

        Node a = barNodes.get(i);
        Node b = barNodes.get(j);

        Bounds aB = a.localToScene(a.getBoundsInLocal());
        Bounds bB = b.localToScene(b.getBoundsInLocal());
        double dxA = bB.getMinX() - aB.getMinX();
        double dxB = aB.getMinX() - bB.getMinX();

        setRole(List.of(i, j), Step.Role.SWAP);

        TranslateTransition ta = new TranslateTransition(Duration.millis(ms), a);
        TranslateTransition tb = new TranslateTransition(Duration.millis(ms), b);
        ta.setByX(dxA);
        tb.setByX(dxB);

        currentSwap = new ParallelTransition(ta, tb);
        currentSwap.setOnFinished(ev -> {
            a.setTranslateX(0);
            b.setTranslateX(0);

            Collections.swap(data, i, j);
            Collections.swap(barNodes, i, j);

            ObservableList<Node> children = barsBox.getChildren();
            Node nodeI = children.get(i);
            Node nodeJ = children.get(j);
            if (i < j) {
                children.remove(j);
                children.remove(i);
                children.add(i, nodeJ);
                children.add(j, nodeI);
            } else {
                children.remove(i);
                children.remove(j);
                children.add(j, nodeI);
                children.add(i, nodeJ);
            }

            clearRoles();
            currentSwap = null;

            if (timeline != null && wasRunning && !pausedByUser && !finished) timeline.play();
        });
        currentSwap.play();
    }

    private void setButtonsIdle() {
        finished = false;
        startButton.setDisable(false);
        pauseButton.setDisable(true);
        unpauseButton.setDisable(true);
        resetButton.setDisable(false);
    }

    private void setButtonsRunning() {
        finished = false;
        startButton.setDisable(true);
        pauseButton.setDisable(false);
        unpauseButton.setDisable(true);
        resetButton.setDisable(true);
    }

    private void setButtonsPaused() {
        finished = false;
        startButton.setDisable(true);
        pauseButton.setDisable(true);
        unpauseButton.setDisable(false);
        resetButton.setDisable(false);
    }

    private void setButtonsFinished() {
        finished = true;
        if (timeline != null) timeline.stop();
        if (currentSwap != null) { currentSwap.stop(); currentSwap = null; }
        startButton.setDisable(true);
        pauseButton.setDisable(true);
        unpauseButton.setDisable(true);
        resetButton.setDisable(false);
    }
}

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.timeline.Frame;
import com.jpexs.decompiler.flash.timeline.Timeline;
import com.jpexs.decompiler.flash.types.MATRIX;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Main extends Application {

    private String referenceDirectoryPath = "";
    private String toChangeDirectoryPath = "";
    private String outputDirectoryPath = "";

    private TextArea console;
    private Button startProcessButton;

    @Override
    public void start(final Stage stage) throws Exception {
        console = new TextArea();

        startProcessButton = new Button("Start");
        startProcessButton.setOnAction(event -> {
            if (!referenceDirectoryPath.isEmpty() && !toChangeDirectoryPath.isEmpty() && !outputDirectoryPath.isEmpty()) {
                startProcessButton.setDisable(true);

                Thread thread = new Thread(this::process);
                thread.start();
            } else {
                printToConsole("All directories have to be selected first");
            }
        });

        HBox referenceContent = setUpSubContent(stage, "Reference");
        HBox toChangeContent = setUpSubContent(stage, "To Change");
        HBox outputContent = setUpSubContent(stage, "Output");

        VBox content = new VBox(15);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(15, 15, 15, 15));
        content.getChildren().addAll(referenceContent, toChangeContent, outputContent, startProcessButton, console);

        Scene scene = new Scene(content, 640, 360);

        stage.setScene(scene);
        stage.show();
    }

    private HBox setUpSubContent(final Stage stage, final String type) {
        HBox content = new HBox(15);

        content.setAlignment(Pos.CENTER);

        TextField textField = new TextField();
        textField.setId(type);
        textField.setPromptText(type);
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            switch (textField.getId()) {
                case "Reference":
                    referenceDirectoryPath = newValue;
                    break;
                case "To Change":
                    toChangeDirectoryPath = newValue;
                    break;
                case "Output":
                    outputDirectoryPath = newValue;
                    break;
            }
        });

        Button directoryChooser = new Button("Choose Directory");
        directoryChooser.setOnAction(event -> {
            File file = new DirectoryChooser().showDialog(stage);

            if (file != null) {
                textField.setText(file.getAbsolutePath());
            }
        });

        content.getChildren().addAll(textField, directoryChooser);

        return content;
    }

    private void process() {
        File referenceDirectory = new File(referenceDirectoryPath);
        File toChangeDirectory = new File(toChangeDirectoryPath);

        Map<String, File[]> files = new HashMap<>();

        try {
            Files.list(Paths.get(toChangeDirectory.toString())).filter(path -> path.toString().endsWith(".swf")).forEach(path -> {
                Path fileName = path.getFileName();
                String fileNameToCheck = fileName.toString();

                if (fileNameToCheck.equals("HUDMenu_2line.swf") || fileNameToCheck.equals("HUDMenu_5line.swf")) {
                    fileNameToCheck = "HUDMenu.swf";
                }

                File toCheck = new File(referenceDirectory + File.separator + fileNameToCheck);

                if (toCheck.exists()) {
                    files.put(fileName.toString(), new File[]{path.toFile(), toCheck});
                } else {
                    printToConsole("No reference file was found for: " + fileName);
                }
            });

            printToConsole("");
        } catch (IOException e) {
            printToConsole(e.toString());
        }

        Set<String> keys = files.keySet();

        for (String key : keys) {
            Map<String, HashMap<String, MATRIX>> referenceData = new HashMap<>();

            File toChange = files.get(key)[0];
            File reference = files.get(key)[1];

            try {
                xMax = -1;
                yMax = -1;
                // Build the reference data
                buildReferenceData(key, referenceData, reference);
                // Overwrite with the reference data
                processSWF(key, referenceData, toChange);
            } catch (IOException | InterruptedException e) {
                printToConsole(e.toString());
            }
        }

        printToConsole("Process is finished");
        printToConsole("");
        Platform.runLater(() -> startProcessButton.setDisable(false));
    }

    private int xMax;
    private int yMax;

    private void buildReferenceData(final String key, final Map<String, HashMap<String, MATRIX>> referenceData, final File file) throws IOException, InterruptedException {
        SWF swf = new SWF(file.toURL().openStream(), file.getAbsolutePath(), key, null, true, false, true, null);

        xMax = swf.displayRect.Xmax;
        yMax = swf.displayRect.Ymax;

        Timeline timeline = swf.getTimeline();

        for (Frame frame : timeline.getFrames()) {
            for (Tag innerTag : frame.innerTags) {
                if (innerTag instanceof PlaceObject2Tag) {
                    PlaceObject2Tag tag = (PlaceObject2Tag) innerTag;

                    HashMap<String, MATRIX> objectData = referenceData.get(frame.toString());

                    if (objectData == null) {
                        objectData = new HashMap<>();
                        objectData.put(tag.getName(), tag.matrix);

                        referenceData.put(frame.toString(), objectData);
                    } else {
                        objectData = referenceData.get(frame.toString());
                        objectData.put(tag.getName(), tag.matrix);
                    }
                }
            }
        }
    }

    private void processSWF(final String key, final Map<String, HashMap<String, MATRIX>> referenceData, final File file) throws IOException, InterruptedException {
        SWF swf = new SWF(file.toURL().openStream(), file.getAbsolutePath(), key, null, true, false, true, null);

        printToConsole(swf.toString());
        printToConsole("New header values (x / y): " + xMax + " / " + yMax);

        swf.displayRect.Xmax = xMax;
        swf.displayRect.Ymax = yMax;
        swf.setModified(true);

        Timeline timeline = swf.getTimeline();

        for (Frame frame : timeline.getFrames()) {
            int count = 0;
            int countReplaced = 0;

            HashMap<String, MATRIX> objectData = referenceData.get(frame.toString());

            if (objectData == null || objectData.isEmpty()) {
                printToConsole("No matrix data found for replacement");

                return;
            }

            for (Tag innerTag : frame.innerTags) {
                if (innerTag instanceof PlaceObject2Tag) {
                    count++;

                    PlaceObject2Tag tag = (PlaceObject2Tag) innerTag;

                    MATRIX matrix = objectData.get(tag.getName());

                    if (matrix == null) {
                        Set<String> keys = objectData.keySet();

                        if (keys.size() > 1) {
                            printToConsole("There are too many object in the frame with different ids - manual process required");
                        } else {
                            for (String newKey : keys) {
                                matrix = objectData.get(newKey);

                                if (matrix != null) {
                                    printToConsole("Object had different id - it should cause no issues though (there was only 1 object avaiable)");
                                    printToConsole("Old ID: <" + newKey + "> | new ID: <" + tag.getName() + ">");
                                } else {
                                    // no need for replacement here - there is no matrix object available
                                    count--;
                                }
                            }
                        }
                    }

                    if (matrix != null) {
                        // Use the changed matrix object
                        tag.matrix = matrix;
                        countReplaced++;
                        tag.setModified(true);
                    }
                }
            }

            printToConsole(frame + " | tags (matrix) replaced: " + countReplaced + " / " + count);
        }

        if (swf.isModified()) {
            printToConsole(referenceData.toString());
            printToConsole("");
            FileOutputStream output = new FileOutputStream(outputDirectoryPath + File.separator + key);
            swf.saveTo(output);
            output.close();
        }
    }

    private void printToConsole(final String text) {
        Platform.runLater(() -> {
            if (console.getText().isEmpty()) {
                console.setText(text);
            } else {
                console.setText(console.getText() + "\n" + text);
            }
        });
    }
}

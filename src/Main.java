import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.timeline.Frame;
import com.jpexs.decompiler.flash.timeline.Timeline;
import com.jpexs.decompiler.flash.types.MATRIX;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Main {

    public static void main(String[] args) {
        new Main();
    }

    public Main() {
        File referenceDirectory = new File("reference");
        File toChangeDirectory = new File("toChange");

        String userDirectory = System.getProperty("user.dir");

        Map<String, File[]> files = new HashMap<>();

        try {
            Files.list(Paths.get(userDirectory + File.separator + toChangeDirectory)).forEach(path -> {
                Path fileName = path.getFileName();
                String fileNameToCheck = fileName.toString();

                if (fileNameToCheck.equals("HUDMenu_2line.swf") || fileNameToCheck.equals("HUDMenu_5line.swf")) {
                    fileNameToCheck = "HUDMenu.swf";
                }

                File toCheck = new File(userDirectory + File.separator + referenceDirectory + File.separator + fileNameToCheck);

                if (toCheck.exists()) {
                    files.put(fileName.toString(), new File[]{path.toFile(), toCheck});
                } else {
                    System.out.println("No reference file was found for: " + fileName);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        Set<String> keys = files.keySet();

        for (String key : keys) {
            Map<String, MATRIX> referenceData = new HashMap<>();

            File toChange = files.get(key)[0];
            File reference = files.get(key)[1];

            try {
                xMax = -1;
                yMax = -1;
                // Build the reference data
                procesSWF(key, referenceData, reference);
                // Overwrite with the reference data
                procesSWF(key, referenceData, toChange);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private int xMax;
    private int yMax;

    private void procesSWF(final String key, final Map<String, MATRIX> referenceData, final File reference) throws IOException, InterruptedException {
        SWF swf = new SWF(reference.toURL().openStream(), reference.getAbsolutePath(), key, null, true, false, true, null);

        if (xMax == -1) {
            xMax = swf.displayRect.Xmax;
            yMax = swf.displayRect.Ymax;
        } else {
            swf.displayRect.Xmax = xMax;
            swf.displayRect.Ymax = yMax;
            swf.setModified(true);
        }

        Timeline timeline = swf.getTimeline();

        for (Frame frame : timeline.getFrames()) {
            for (Tag innerTag : frame.innerTags) {
                if (innerTag instanceof PlaceObject2Tag) {
                    PlaceObject2Tag tag = (PlaceObject2Tag) innerTag;

                    MATRIX matrix = referenceData.get(tag.getName());

                    if (matrix == null) {
                        referenceData.put(tag.getName(), tag.matrix);
                    } else {
                        // Use the changed matrix object
                        tag.matrix = matrix;
                        tag.setModified(true);
                    }
                }
            }
        }

        if (swf.isModified()) {
            System.out.println(swf);
            System.out.println(referenceData);
            System.out.println();

            FileOutputStream output = new FileOutputStream("output" + File.separator + key);
            swf.saveTo(output);
            output.close();
        }
    }
}

import java.io.*;

/**
 * Created by sank on 2/9/17.
 */
public class Utils {
    public static void main(String[] args) throws IOException {
        changeGPULoad("/Users/sank/Desktop/test", 0.91d, 3d);
    }

    public static double getTimePerTimeStepValue(String fileName) {
        double time = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line = br.readLine();
            line = br.readLine();
            while ((line = br.readLine()) != null) {
                if (line.contains("time/timestep                  :")) {
                    time = Double.parseDouble(line.split(":")[1].trim().split(" ")[0]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return time;
    }

    public static void changeGPULoad(String fileName, Double gpuload, Double cpu2gpu) throws IOException {
        File file = new File(fileName);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = "", newtext = "";
        while ((line = reader.readLine()) != null) {
            if (line.contains("p070")) {
                newtext += "  " + String.format("%12.2E", cpu2gpu) + " p070 cpu2gpu\r\n";
            } else if (line.contains("p071")) {
                newtext += "  " + String.format("%12.2E", gpuload) + " p071 gpuload\r\n";
            } else {
                newtext += line + "\r\n";
            }
        }
        reader.close();
        FileWriter writer = new FileWriter(fileName);
        writer.write(newtext);
        writer.close();
    }
}

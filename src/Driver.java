import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created by sank on 2/9/17.
 */
public class Driver {

    static Double lowGPUload = 0.2d;
    static Double highGPUload = 0.99d;
    static Double cpu2gpu = 3d;
    static String nek5000Path = "/users/sankee168/CMTHybrid-master/nek5/examples/3dboxHybrid/";
    static int processes = 4;
    static String logLocation = "/users/sankee168/BinarySearchLogs/";
    static boolean isDualSocket = false;

    private static final Logger LOGGER = Logger.getLogger(Driver.class.getName());

    public static void main(String[] args) throws IOException, InterruptedException {
        LOGGER.info("Staring run for cpu2gpu : " + cpu2gpu + " and processes : " + processes);
        binarySearch(lowGPUload, highGPUload);
    }

    public static double executeNek5000(String nek5000Path, int processes, boolean isDualSocket, Double gpuload, Double cpu2gpu, String logLocation) throws IOException, InterruptedException {
        Utils utils = new Utils();
        utils.changeGPULoad(nek5000Path + "box.rea", gpuload, cpu2gpu);
        LOGGER.info("Changed box.rea file with gpuload : " + gpuload + " cpu2gpu : " + cpu2gpu);
        StringBuilder sb = new StringBuilder("");
        sb.append("cd " + nek5000Path + "; ");
        String log = logLocation + "hybrid/" + gpuload + "_" + cpu2gpu + "_" + processes + ".log";
        LOGGER.info("Log location : " + log);
        if (isDualSocket) {
            sb.append("mpirun --allow-run-as-root --report-bindings -n " + processes + "./nek5000 > " + log);
        } else {
            sb.append("mpirun --allow-run-as-root --map-by core --report-bindings -n " + processes + "./nek5000 > " + log);
        }
        LOGGER.info("Command executing : " + sb.toString());
        Process process = Runtime.getRuntime().exec(sb.toString());
        process.waitFor();

        double time = utils.getTimePerTimeStepValue(log);
        LOGGER.info("Time per 10 timestep : " + time*10);
        return time*10;
    }


    public static Double binarySearch(Double low, Double high) throws IOException, InterruptedException {
        LOGGER.info("Initialising binary search for gpuload low : " + low + " high : " + high);
        if (low > high){
            return 0d;
        }else if(Math.abs(low-high) <= 0.01){
            LOGGER.info("Entering final binary search for gpuload low : " + low + " high : " + high);
            double templow = executeNek5000(nek5000Path, processes, isDualSocket, low, cpu2gpu, logLocation);
            double temphigh = executeNek5000(nek5000Path, processes, isDualSocket, high, cpu2gpu, logLocation);
            if(templow < temphigh){
                LOGGER.info("Optimal gpuload for cpu2gpu : " + cpu2gpu + " and processes :" + processes + " is " + low);
                return low;
            }
            LOGGER.info("Optimal gpuload for cpu2gpu : " + cpu2gpu + " and processes :" + processes + " is " + high);
            return high;
        }

        Double mid = (low + high) / 2;

        double lowerTime = executeNek5000(nek5000Path, processes, isDualSocket, mid-0.05, cpu2gpu, logLocation);
        double higherTime = executeNek5000(nek5000Path, processes, isDualSocket, mid+0.05, cpu2gpu, logLocation);
        double midTime = executeNek5000(nek5000Path, processes, isDualSocket, mid, cpu2gpu, logLocation);

        if (lowerTime < midTime && midTime < higherTime)
            return binarySearch(low, mid-0.01);
        else if (lowerTime > midTime && midTime > higherTime)
            return binarySearch(mid+0.01, high);
        else
            return binarySearch(mid-0.05, mid+0.05);
    }


}

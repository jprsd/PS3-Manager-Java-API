/*
 * Lightweight Java/Android PS3 Manager API
 * Author: Jaideep Prasad
 * Date: 2020
 *
 * This code may be freely used or modified for
 * any purpose provided this notice is included
 * in all versions. Special thanks to aldostools
 * and _NzV_ for their open source contributions
 * to webMAN MOD and the original .NET PS3MAPI.
 */

package ps3.mods.jprsd.ps3mapi;

import okhttp3.ResponseBody;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Lightweight PS3 Manager API for use in Java/Android applications
 * @author Jaideep Prasad
 */
public class PS3MAPI {

    //region WebMAN MOD local PS3 server paths
    static final String HOME_PATH = "home.ps3mapi";
    static final String SHUTDOWN_PATH = "shutdown.ps3";
    static final String REBOOT_PATH = "reboot.ps3";
    static final String NOTIFY_PATH = "notify.ps3mapi";
    static final String LED_PATH = "led.ps3mapi";
    static final String BUZZER_PATH = "buzzer.ps3mapi";
    static final String ID_PATH = "setidps.ps3mapi";
    static final String SET_MEMORY_PATH = "setmem.ps3mapi";
    static final String GET_MEMORY_PATH = "getmem.ps3mapi";
    //endregion

    // REST client
    private Retrofit retrofit;
    // Interface for web commands
    private PS3Service ps3Service;

    // PS3 IP address
    private String ipAddress;
    // Attached PS3 process ID
    private long processId;

    /**
     * Default constructor
     */
    public PS3MAPI() {
        disconnect();
    }

    /**
     * @return PS3 IP address (or empty string if not set)
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * @param ipAddress The PS3 IP address
     * @throws Exception if an invalid address is provided
     */
    public void setIpAddress(String ipAddress) throws Exception {
        this.ipAddress = ipAddress;
        String baseUrl = "http://" + ipAddress + "/";
        retrofit = new Retrofit.Builder().baseUrl(baseUrl).build();
    }

    /**
     * @return Attached PS3 process ID (or -1 if no process is attached)
     */
    public long getProcessId() {
        return processId;
    }

    /**
     * Manually set the process ID for attachment
     * @param processId The PS3 process ID
     * @see #attach()
     */
    public void setProcessId(long processId) {
        this.processId = processId;
    }

    /**
     * Disconnects from the PS3
     */
    public void disconnect() {
        retrofit = null;
        ps3Service = null;
        ipAddress = "";
        processId = -1;
    }

    /**
     * Establishes a connection to the PS3
     * @return true if connection is successful
     * @apiNote This method runs synchronously and should be managed in a non-UI thread
     */
    public boolean connect() {
        try {
            ps3Service = retrofit.create(PS3Service.class);
            return notify("PS3MAPI Device Connected");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Shuts down the PS3
     * @return true if shutdown is successful
     * @apiNote This method runs synchronously and should be managed in a non-UI thread
     */
    public boolean shutdown() {
        try {
            Response<ResponseBody> response = ps3Service.shutdown().execute();
            if (response.body() != null) {
                disconnect();
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Hard reboots the PS3
     * @return true if the reboot is successful
     * @apiNote This method runs synchronously and should be managed in a non-UI thread
     */
    public boolean reboot() {
        try {
            Response<ResponseBody> response = ps3Service.reboot().execute();
            if (response.body() != null) {
                disconnect();
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Sends a message to be displayed by the PS3
     * @param message The message to send
     * @return true if the notification is successful
     * @apiNote This method runs synchronously and should be managed in a non-UI thread
     */
    public boolean notify(String message) {
        try {
            Response<ResponseBody> response = ps3Service.notify(message).execute();
            return response.body() != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Controls the PS3 LEDs
     * @param color Light color
     * @param mode Flash mode
     * @return true if the request is successful
     * @apiNote This method runs synchronously and should be managed in a non-UI thread
     */
    public boolean setLights(PS3Light.Color color, PS3Light.Mode mode) {
        try {
            Response<ResponseBody> response =
                    ps3Service.setLights(color.getValue(), mode.getValue()).execute();
            return response.body() != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Rings the PS3 buzzer
     * @param mode Beeping mode
     * @return true if the request is successful
     * @apiNote This method runs synchronously and should be managed in a non-UI thread
     */
    public boolean ringBuzzer(PS3Buzzer.Mode mode) {
        try {
            Response<ResponseBody> response = ps3Service.ringBuzzer(mode.getValue()).execute();
            return response.body() != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Sets the PS3 IDPS and PSID to the given values
     * @param idps1 First 16 characters of the IDPS
     * @param idps2 Last 16 characters of the IDPS
     * @param psid1 First 16 characters of the PSID
     * @param psid2 Last 16 characters of the PSID
     * @return true if the request is successful
     * @apiNote This method runs synchronously and should be managed in a non-UI thread
     */
    public boolean setIdpsAndPsid(String idps1, String idps2, String psid1, String psid2) {
        if (idps1.length() != 16 || idps2.length() != 16
                || psid1.length() != 16 || psid2.length() != 16
                || !isHexString(idps1 + idps2 + psid1 + psid2)) {
            return false;
        }
        try {
            Response<ResponseBody> response =
                    ps3Service.setIdpsAndPsid(idps1, idps2, psid1, psid2).execute();
            return response.body() != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Searches for an EBOOT process and automatically attaches to it if found
     * @return true if attached successfully
     * @apiNote This method runs synchronously and should be managed in a non-UI thread
     */
    public boolean attach() {
        try {
            Response<ResponseBody> response = ps3Service.attach().execute();
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                return false;
            }
            String html = responseBody.string();
            Document document = Jsoup.parse(html);
            Elements selects = document.getElementsByTag("select");
            for (Element select : selects) {
                int processes = select.childNodes().size();
                if (processes < 2) continue;
                for (int i = 0; i < processes; i += 2) {
                    if (((TextNode)select.childNode(i + 1)).text().contains("EBOOT")) {
                        setProcessId(Long.parseLong(select.childNode(i).attr("value")));
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Writes to a memory address on the PS3
     * @param address Memory address hex string
     * @param value Value hex string
     * @return true if the memory was set successfully
     * @apiNote This method runs synchronously and should be managed in a non-UI thread
     */
    public boolean setMemory(String address, String value) {
        if (!isHexString(address + value)) {
            return false;
        }
        try {
            Response<ResponseBody> response =
                    ps3Service.setMemory(processId, address, value).execute();
            return response.body() != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Reads from a memory address on the PS3
     * @param address Memory address hex string
     * @param length Number of bytes to read (must be greater than 0)
     * @param result User-provided String array of at least size 1 to store the resulting value
     * @return true if the memory was read successfully
     * @apiNote This method runs synchronously and should be managed in a non-UI thread
     */
    public boolean getMemory(String address, int length, String[] result) {
        if (!isHexString(address) || length <= 0) {
            return false;
        }
        try {
            Response<ResponseBody> response =
                    ps3Service.getMemory(processId, address, length).execute();
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                return false;
            }
            String html = responseBody.string();
            Document document = Jsoup.parse(html);
            result[0] = document.getElementById("output").val();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Determines if a given string contains only hexadecimal characters
     * @param string The string to evaluate
     * @return true if the string is a hex string
     */
    private static boolean isHexString(String string) {
        String str = string.toUpperCase();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (!Character.isLetterOrDigit(c) || (Character.isLetter(c) && c > 'F')) {
                return false;
            }
        }
        return true;
    }

    /**
     * Class that contains enumerations related to PS3 LED functionality
     */
    public static final class PS3Light {

        /**
         * PS3 LED color options
         */
        public enum Color {
            RED(0), GREEN(1), YELLOW(2);
            private final int value;
            Color(int value) { this.value = value; }
            int getValue() { return value; }
        }

        /**
         * PS3 LED mode options
         */
        public enum Mode {
            OFF(0), ON(1), BLINK_FAST(2), BLINK_SLOW(3);
            private final int value;
            Mode(int value) { this.value = value; }
            int getValue() { return value; }
        }

    }

    /**
     * Class that contains enumerations related to PS3 buzzer functionality
     */
    public static final class PS3Buzzer {

        /**
         * PS3 buzzer mode options
         */
        public enum Mode {
            SINGLE(1), DOUBLE(2), TRIPLE(3);
            private final int value;
            Mode(int value) { this.value = value; }
            int getValue() { return value; }
        }

    }

}

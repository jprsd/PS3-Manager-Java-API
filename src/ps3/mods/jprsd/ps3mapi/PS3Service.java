/*
 * PS3 Service interface for web commands
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
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import static ps3.mods.jprsd.ps3mapi.PS3MAPI.HOME_PATH;
import static ps3.mods.jprsd.ps3mapi.PS3MAPI.SHUTDOWN_PATH;
import static ps3.mods.jprsd.ps3mapi.PS3MAPI.REBOOT_PATH;
import static ps3.mods.jprsd.ps3mapi.PS3MAPI.NOTIFY_PATH;
import static ps3.mods.jprsd.ps3mapi.PS3MAPI.LED_PATH;
import static ps3.mods.jprsd.ps3mapi.PS3MAPI.BUZZER_PATH;
import static ps3.mods.jprsd.ps3mapi.PS3MAPI.ID_PATH;
import static ps3.mods.jprsd.ps3mapi.PS3MAPI.SET_MEMORY_PATH;
import static ps3.mods.jprsd.ps3mapi.PS3MAPI.GET_MEMORY_PATH;

/**
 * PS3 Service interface for web commands
 * @author Jaideep Prasad
 */
interface PS3Service {

    /**
     * Sends a request to retrieve system process information for attachment
     * @return Parsable response body
     */
    @GET(HOME_PATH)
    Call<ResponseBody> attach();

    /**
     * Sends a request to turn off the system
     * @return Parsable response body
     */
    @GET(SHUTDOWN_PATH)
    Call<ResponseBody> shutdown();

    /**
     * Sends a request to reboot the system
     * @return Parsable response body
     */
    @GET(REBOOT_PATH)
    Call<ResponseBody> reboot();

    /**
     * Sends a request to display a message
     * @return Parsable response body
     */
    @GET(NOTIFY_PATH)
    Call<ResponseBody> notify(@Query("msg") String message);

    /**
     * Sends a request to adjust LED settings
     * @return Parsable response body
     */
    @GET(LED_PATH)
    Call<ResponseBody> setLights(
            @Query("color") int color,
            @Query("mode") int mode
    );

    /**
     * Sends a request to ring the buzzer
     * @return Parsable response body
     */
    @GET(BUZZER_PATH)
    Call<ResponseBody> ringBuzzer(@Query("mode") int mode);

    /**
     * Sends a request to change the system IDPS and PSID
     * @return Parsable response body
     */
    @GET(ID_PATH)
    Call<ResponseBody> setIdpsAndPsid(
            @Query("idps1") String idps1,
            @Query("idps2") String idps2,
            @Query("psid1") String psid1,
            @Query("psid2") String psid2
    );

    /**
     * Sends a request to write to system memory
     * @return Parsable response body
     */
    @GET(SET_MEMORY_PATH)
    Call<ResponseBody> setMemory(
            @Query("proc") long processId,
            @Query("addr") String address,
            @Query("val") String value
    );

    /**
     * Sends a request to read from system memory
     * @return Parsable response body
     */
    @GET(GET_MEMORY_PATH)
    Call<ResponseBody> getMemory(
            @Query("proc") long processId,
            @Query("addr") String address,
            @Query("len") int length
    );

}

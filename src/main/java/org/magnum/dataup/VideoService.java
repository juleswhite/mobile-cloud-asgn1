package org.magnum.dataup;

import org.magnum.dataup.model.Video;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class VideoService {

    static final AtomicLong currentID = new AtomicLong(0L);
    Map<Long,Video> videoMaps = new HashMap<Long, Video>();
    List<Video> videoLists = new ArrayList<Video>();

    /**
     * 1. Create videoID by calling SetId() method. [ID should not be 0].
     * 2. Create a Map<Long Id,Video v>. Put video object along with ID as a key inside Map.
     * 3. Generate the full URL by passing ID.
     * 4. Create a collection class and store video object inside the collection.
     * 5. Return video object.
     * @param video
     * @return
     */
    public Video postVideo(Video video){
        checkAndSetId(video);
        videoMaps.put(video.getId(),video);
        String dataURL = getDataUrl(video.getId());
        video.setDataUrl(dataURL);

        videoLists.add(video);
        return video;
    }

    /**
     * Impl step 1. Method to creat ID for video.
     */
    public void checkAndSetId(Video entity) {
        if(entity.getId() == 0){
            entity.setId(currentID.incrementAndGet());
        }
    }

    /**
     * Impl Step 3. Pass videoID as an args. Return VideoURL as a String Param.
     * @param videoId
     * @return
     */
    public String getDataUrl(long videoId){
        String url = getUrlBaseForLocalServer() + "/video/" + videoId + "/data";
        return url;
    }

    private String getUrlBaseForLocalServer() {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String base =
                "http://"+request.getServerName()
                        + ((request.getServerPort() != 80) ? ":"+request.getServerPort() : "");
        return base;
    }

    //============================================================================
    //============================================================================

    /**
     * 6. Return Collection of video.
     * @return
     */
    public Collection<Video> getVideoList(){
        return videoLists;
    }


    //============================================================================
    //============================================================================

    /**
     * 7. Create POST method to upload Video Binary data.
     */

}

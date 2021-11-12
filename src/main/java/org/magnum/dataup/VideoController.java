package org.magnum.dataup;


import org.magnum.dataup.model.Video;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
public class VideoController {

    Map<Long, Video> videos = new HashMap<Long, Video>();
    static final AtomicLong currentId = new AtomicLong(0L);

    @RequestMapping(value = "/video", method = GET)
    public @ResponseBody
    List<Video> getSavedVideos() {
        return new ArrayList<>(videos.values());
    }

    @RequestMapping(value = "/video", method = POST)
    public @ResponseBody
    Video postVideo(@RequestBody Video video) {
        video.setDataUrl(getDataUrl(video.getId()));
        return save(video);
    }

    private String getDataUrl(long videoId) {
        String url = getUrlBaseForLocalServer() + "/video/" + videoId + "/data";
        return url;
    }

    private String getUrlBaseForLocalServer() {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String base =
                "http://" + request.getServerName()
                        + ((request.getServerPort() != 80) ? ":" + request.getServerPort() : "");
        return base;
    }

    public Video save(Video entity) {
        checkAndSetId(entity);
        videos.put(entity.getId(), entity);
        return entity;
    }

    private void checkAndSetId(Video entity) {
        if (entity.getId() == 0) {
            entity.setId(currentId.incrementAndGet());
        }
    }


}

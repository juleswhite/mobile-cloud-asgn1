package org.magnum.dataup;


import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
public class VideoController {

    static Logger log = LoggerFactory.getLogger(VideoController.class);
    Map<Long, Video> videos = new HashMap<>();
    static final AtomicLong currentId = new AtomicLong(0L);
    static VideoFileManager videoFileManager;

    static {
        try {
            videoFileManager = VideoFileManager.get();
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.toString());
        }
    }

    @RequestMapping(value = "/video", method = GET)
    public @ResponseBody
    List<Video> getSavedVideos() {
        return new ArrayList<>(videos.values());
    }

    @RequestMapping(value = "/video", method = POST)
    public @ResponseBody
    Video postVideoMetaData(@RequestBody Video video) {
        video.setDataUrl(getDataUrl(video.getId()));
        return save(video);
    }

    @RequestMapping(value = "/video/{id}/data", method = POST)
    public @ResponseBody
    VideoStatus postVideoFile(@PathVariable("id") long id,
                              @RequestParam("data") MultipartFile videoData,
                              HttpServletResponse response) throws IOException {
        if (videos.get(id) != null) {
            videoFileManager.saveVideoData(videos.get(id), videoData.getInputStream());
            return new VideoStatus(VideoStatus.VideoState.READY);
        }
        response.setStatus(404);
        return null;
    }


    @RequestMapping(value = "/video/{id}/data", method = GET)
    public @ResponseBody
    void streamVideoFile(@PathVariable("id") long id,
                         HttpServletResponse response) throws IOException {
        if (videos.get(id) != null) {
            //we simply write to the output stream, we do not return any object as response
            videoFileManager.copyVideoData(videos.get(id), response.getOutputStream());
        } else {
            response.setStatus(404);
        }
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

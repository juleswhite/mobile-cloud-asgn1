package org.magnum.dataup;


import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    ResponseEntity<List<Video>> getSavedVideos() {
        try {
            return new ResponseEntity<>(new ArrayList<>(videos.values()), HttpStatus.ACCEPTED);
        } catch (Exception ex) {
            log.error(ex.toString());
            //ex.printStackTrace();
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/video", method = POST)
    public @ResponseBody
    ResponseEntity<Video> postVideoMetaData(@RequestBody Video video) {
        try {
            video.setDataUrl(getDataUrl(video.getId()));
            return new ResponseEntity<>(save(video), HttpStatus.ACCEPTED);
        } catch (Exception ex) {
            log.error(ex.toString());
            //ex.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/video/{id}/data", method = POST)
    public @ResponseBody
    ResponseEntity<VideoStatus> postVideoFile(@PathVariable("id") long id,
                                              @RequestParam("data") MultipartFile videoData,
                                              HttpServletResponse response) {
        try {
            if (videos.get(id) != null) {
                videoFileManager.saveVideoData(videos.get(id), videoData.getInputStream());
                return new ResponseEntity<>(new VideoStatus(VideoStatus.VideoState.READY), HttpStatus.ACCEPTED);
            }
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            log.error(ex.toString());
            //ex.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @RequestMapping(value = "/video/{id}/data", method = GET)
    public @ResponseBody
    void streamVideoFile(@PathVariable("id") long id,
                         HttpServletResponse response) {
        try {
            if (videos.get(id) != null) {
                //we simply write to the output stream, we do not return any object as response
                videoFileManager.copyVideoData(videos.get(id), response.getOutputStream());
                response.setStatus(HttpStatus.ACCEPTED.value());
            } else {
                response.setStatus(HttpStatus.NOT_FOUND.value());
            }
        } catch (Exception ex) {
            log.error(ex.toString());
            //ex.printStackTrace();
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private String getDataUrl(long videoId) {
        return getUrlBaseForLocalServer() + "/video/" + videoId + "/data";
    }

    private String getUrlBaseForLocalServer() {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        return "http://" + request.getServerName()
                + ((request.getServerPort() != 80) ? ":" + request.getServerPort() : "");
    }

    private Video save(Video entity) {
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

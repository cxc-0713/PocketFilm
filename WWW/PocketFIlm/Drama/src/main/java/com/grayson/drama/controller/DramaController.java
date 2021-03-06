package com.grayson.drama.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.grayson.common.config.Configs;
import com.grayson.common.util.CommonUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/")
public class DramaController {

    /**
     * 获取推荐影视数据
     *
     * @param map 数据映射
     * @return 首页
     */
    @RequestMapping("/")
    public String getMovie(ModelMap map, HttpServletRequest request) {
        //  获取用户名
        CommonUtils commonUtils = new CommonUtils();
        String cookieName = "userInfo";
        JSONObject userInfo = commonUtils.getCookieValue(request, cookieName);
        String username = null;
        if (userInfo != null) {
            username = userInfo.getString("username");
        }

        //  获取浏览记录
        JSONArray records = commonUtils.getRecords(request, "drama");

        map.addAttribute("username", username);
        map.addAttribute("records", records);
        map.addAttribute("title", "掌上戏曲_免费在线观看京剧豫剧越剧秦腔民间小调二人转");
        return "movie.html";
    }

    /**
     * 获取渲染数据
     *
     * @param map 数据映射
     * @return 首页中心页面
     */
    @RequestMapping("/main")
    public String getMain(ModelMap map, HttpServletRequest request) {
        CommonUtils commonUtils = new CommonUtils();
        Integer pageSize = 18;
        //  热门推荐
        String hottestMoviesUrl = Configs.API + "/drama/get/all?page_size=" + pageSize;
        JSONObject hottestMoviesObject = commonUtils.doGet(hottestMoviesUrl);
        map.addAttribute("hottestMovies", hottestMoviesObject.getJSONArray("data"));

        //  京剧
        String movies0Url = Configs.API + "/drama/get/all?type=京剧&page_size=" + pageSize;
        //  豫剧
        String movies1Url = Configs.API + "/drama/get/all?type=豫剧&page_size=" + pageSize;
        //  秦腔
        String movies2Url = Configs.API + "/drama/get/all?type=秦腔&page_size=" + pageSize;
        //  民间小调
        String movies3Url = Configs.API + "/drama/get/all?type=民间小调&page_size=" + pageSize;
        //  今日更新
        String todayMoviesUrl = Configs.API + "/get/today?type=drama";
        //  今日更新数据量
        String todayCountUrl = Configs.API + "/count/get/today?type=drama";
        JSONObject movies0Object = commonUtils.doGet(movies0Url);
        JSONObject movies1Object = commonUtils.doGet(movies1Url);
        JSONObject movies2Object = commonUtils.doGet(movies2Url);
        JSONObject movies3Object = commonUtils.doGet(movies3Url);
        JSONObject todayMoviesObject = commonUtils.doGet(todayMoviesUrl);
        JSONObject todayCountObject = commonUtils.doGet(todayCountUrl);
        map.addAttribute("movies0", movies0Object.getJSONArray("data"));
        map.addAttribute("movies1", movies1Object.getJSONArray("data"));
        map.addAttribute("movies2", movies2Object.getJSONArray("data"));
        map.addAttribute("movies3", movies3Object.getJSONArray("data"));
        map.addAttribute("todayMovies", todayMoviesObject.getJSONArray("data"));
        map.addAttribute("todayCount", todayCountObject.getInteger("data"));
        return "main.html";
    }

    /**
     * 播放影视
     *
     * @param map 数据映射
     * @param _id 影视_id
     * @return 播放页面
     */
    @RequestMapping("/play")
    public String playMovie(ModelMap map, HttpServletRequest request, @RequestParam("_id") String _id, @RequestParam("source_index") Integer sourceIndex, @RequestParam("type_index") Integer typeIndex) {
        //  获取用户名
        CommonUtils commonUtils = new CommonUtils();
        String cookieName = "userInfo";
        JSONObject userInfo = commonUtils.getCookieValue(request, cookieName);
        String username = null;
        if (userInfo != null) {
            username = userInfo.getString("username");
        }
        map.addAttribute("username", username);

        String url = Configs.API + "/drama/get/_id?_id=" + _id;
        JSONObject jsonObject = commonUtils.doGet(url);
        JSONObject movie = jsonObject.getJSONObject("data");
        map.addAttribute("movie", movie);
        map.put("source_index", sourceIndex);
        map.put("type_index", typeIndex);
        JSONObject sourceObject = jsonObject.getJSONObject("data").getJSONArray("sources").getJSONObject(sourceIndex);
        map.put("source", sourceObject);
        String currentUrl = sourceObject.getJSONArray("types").getJSONObject(typeIndex).getString("url");
        String playUrl = commonUtils.getParseUrl("drama", currentUrl);
        map.put("play_url", playUrl);
        map.put("title", "《" + movie.get("name") + "》免费在线观看-掌上戏曲免费在线观看高清戏曲直播" + movie.get("name"));

        //  记录浏览历史
        JSONObject recordsToRecordObject = new JSONObject();
        recordsToRecordObject.put("user_name", username);
        recordsToRecordObject.put("id", _id);
        recordsToRecordObject.put("name", movie.getString("name"));
        recordsToRecordObject.put("type", movie.getString("type"));
        recordsToRecordObject.put("type2", "null");
        recordsToRecordObject.put("src", movie.getString("src"));
        recordsToRecordObject.put("url", jsonObject.getJSONObject("data").getJSONArray("sources").getJSONObject(sourceIndex).getJSONArray("types").getJSONObject(typeIndex).getString("url"));
        recordsToRecordObject.put("browse_type", "drama");
        recordsToRecordObject.put("device_uuid", request.getHeader("user-agent"));
        recordsToRecordObject.put("device_version", System.getProperty("os.version"));
        recordsToRecordObject.put("device_platform", System.getProperty("os.name"));
        String recordsUrl = Configs.API + "/records/add";
        commonUtils.doPost(recordsUrl, recordsToRecordObject);

        return "play.html";
    }

    /**
     * 获取更多影视
     *
     * @param map        数据映射
     * @param type       影视第一种类型
     * @param page_index 当前页
     * @param key_word   关键词
     * @return 影视数据
     */
    @RequestMapping("/more")
    public String getMoreMovie(ModelMap map, HttpServletRequest request, @RequestParam(value = "type", defaultValue = "全部") String type, @RequestParam(value = "page_index", defaultValue = "1") String page_index, @RequestParam(value = "key_word", defaultValue = "") String key_word) {
        //  获取用户名
        CommonUtils commonUtils = new CommonUtils();
        if (key_word.equals("null")) {
            key_word = "";
        }
        String cookieName = "userInfo";
        JSONObject userInfo = commonUtils.getCookieValue(request, cookieName);
        String username = null;
        if (userInfo != null) {
            username = userInfo.getString("username");
        }
        map.addAttribute("username", username);

        //  获取影视类型
        String typeUrl = Configs.API + "/drama/type/get/all";
        JSONObject typeObject = commonUtils.doGet(typeUrl);
        JSONArray movieTypes = new JSONArray();
        movieTypes.add("全部");
        typeObject.getJSONArray("data").forEach(object -> {
            object = (JSONObject)object;
            movieTypes.add(((JSONObject) object).get("name"));
        });

        map.addAttribute("movieTypes", movieTypes);
        map.addAttribute("type", type);
        map.addAttribute("key_word", key_word);
        map.addAttribute("title", type + "频道第" + page_index + "页-免费在线观看-掌上戏曲");
        return "more-movie.html";
    }

    /**
     * 搜索影视
     *
     * @param map        数据映射
     * @param page_index 当前页
     * @param sort_type  排序方式
     * @param key_word   关键词
     * @return 搜索结果页
     */
    @RequestMapping("/search")
    public String searchMovie(ModelMap map, HttpServletRequest request, @RequestParam(value = "page_index", defaultValue = "1") String page_index, @RequestParam(value = "sort_type", defaultValue = "0") String sort_type, @RequestParam(value = "key_word", defaultValue = "null") String key_word) {
        //  获取用户名
        CommonUtils commonUtils = new CommonUtils();
        String cookieName = "userInfo";
        JSONObject userInfo = commonUtils.getCookieValue(request, cookieName);
        String username = null;
        if (userInfo != null) {
            username = userInfo.getString("username");
        }
        map.addAttribute("username", username);
        map.addAttribute("key_word", key_word);
        map.addAttribute("title", key_word + "-掌上戏曲戏曲搜索");
        return "search-movie.html";

    }

}

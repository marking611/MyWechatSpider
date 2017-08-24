package com.mak.util;


import com.mak.domain.Content;
import com.mak.exception.WechatException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WechatUtil {

    private final static Logger LOGGER = LoggerFactory.getLogger(WechatUtil.class);

    private String id;
    protected Content model;
    private int totalpages = 0;
    private String sogouParam = "";

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return "1.0";
    }

    public String getId() {
        return this.id;
    }

    private int getTotalPage(String str) {
        if (0 != totalpages) {
            return totalpages;
        }
        Pattern pattern = Pattern.compile("totalPages\":([0-9]*)");
        Matcher matcher = pattern.matcher(str);

        while (matcher.find()) {
            totalpages = Integer.parseInt(matcher.group(1));
        }
        return totalpages;
    }

    /**
     * 获取第一页的doc对象
     *
     * @return
     */
    protected Document getDoc() {

        String url = makeUrl();
        try {
            return Jsoup
                    .connect(url)
                    .timeout(10000)
                    .ignoreContentType(true)
                    .header("User-Agent",
                            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.131 Safari/537.36")
                    .header("Cookie",
                            "ABTEST=8|1430710665|v1; SUID=F55370722708930A000000005546E989; PHPSESSID=0hk2d8cl4128niajvb4f4asfq6; SUIR=1430710665; SUID=F55370724FC80D0A000000005546E989; SNUID=D47250532024351871AD39CB21F3D59C; SUV=00EA70CE727053F55546F1207367B700; weixinIndexVisited=1; wuid=AAGjZr7TCQAAAAqUKHWrjwEAkwA=; ld=nAVZ9yllll2qSs4glllllVqpDNtllllltXxFdyllll9lllllxllll5@@@@@@@@@@; usid=pz2gIdtBRiERY8lB; sct=2; wapsogou_qq_nickname=; IPLOC=CN3200")
                    .get();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 获取指定url的doc对象
     *
     * @param url
     * @return
     */
    protected static Document getDoc(String url) {
        try {
            return Jsoup
                    .connect(url)
                    .ignoreContentType(true)
                    .timeout(10000)
                    .header("User-Agent",
                            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.131 Safari/537.36")
                    .header("Cookie",
                            "ABTEST=8|1430710665|v1; SUID=F55370722708930A000000005546E989; PHPSESSID=0hk2d8cl4128niajvb4f4asfq6; SUIR=1430710665; SUID=F55370724FC80D0A000000005546E989; SNUID=D47250532024351871AD39CB21F3D59C; SUV=00EA70CE727053F55546F1207367B700; weixinIndexVisited=1; wuid=AAGjZr7TCQAAAAqUKHWrjwEAkwA=; ld=nAVZ9yllll2qSs4glllllVqpDNtllllltXxFdyllll9lllllxllll5@@@@@@@@@@; usid=pz2gIdtBRiERY8lB; sct=2; wapsogou_qq_nickname=; IPLOC=CN3200")
                    .get();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取搜狗url的参数
     */
    public String getSogouParam() {
        if (!"".equals(this.sogouParam)) {
            return this.sogouParam;
        }
        ScriptEngineManager sem = new ScriptEngineManager();
        ScriptEngine se = sem.getEngineByExtension("js");
        try {
            se.eval(new FileReader(this.getClass().getResource("/url.js")
                    .getPath()));
            se.eval("eval(\"window.SogouEncrypt.setKv('8d11ae022be','1')\")");
            this.sogouParam = (String) se
                    .eval("eval(\"window.SogouEncrypt.encryptquery('"
                            + this.getId() + "','sogou')\")");
        } catch (FileNotFoundException | ScriptException e) {
            System.out.println(e);
        }
        return this.sogouParam;
    }

    protected String makeUrl() {
        if (null == id || "".equals(id)) {
            throw new WechatException("must set id first");
        }
        String urlParams = this.getSogouParam();
        return "http://weixin.sogou.com/gzhjs?cb=sogou.weixin.gzhcb&" + urlParams;
    }

    protected String makeUrl(int page) {
        if (null == id || "".equals(id)) {
            throw new WechatException("must set id first");
        }
        String urlParams = this.getSogouParam();
        return "http://weixin.sogou.com/gzhjs?cb=sogou.weixin.gzhcb&" + urlParams + "&page=" + page;
    }

    protected void excute() {
        Document doc = getDoc();
        if (null == doc) {
            throw new WechatException("unknown error");
        }

        Element topicUrl = doc.select("url").first();
        if (null == topicUrl) {
            throw new WechatException(
                    "make sure the openId is right, otherwise no topcs in this wechat account");
        }
        topicUrl.select("title1").remove();
        String url = topicUrl.text();
        fetchContent(url);
    }

    protected void fetchContent(String url) {
        Document doc = getDoc(url);
        if (null == doc) {
            return;
        }
        model = new Content();
        String title = doc.select("#activity-name").first().text();
        Elements imagesDom = doc.select("#js_content img[data-src]");
        String content = doc.select("#js_content").first().html();
        String date = doc.select("#post-date").first().text();
        String user = doc.select("#post-user").first().text();
        List<String> images = new ArrayList<>();
        for (Element img : imagesDom) {
            images.add(img.attr("data-src"));
        }

        model.setContent(content);
//        model.setImages(images);
        model.setUrl(url);
        model.setTitle(title);
        model.setDate(date);
        model.setUser(user);
    }

    public static Content getTopicByUrl(String url) {
        Document doc = getDoc(url);
        if (null == doc) {
            return null;
        }
        Content topic = new Content();
        String title = doc.select("#activity-name").first().text();
        Elements imagesDom = doc.select("#js_content img[data-src]");
        String content = doc.select("#js_content").first().html();
        String date = doc.select("#post-date").first().text();
        String user = doc.select("#post-user").first().text();
        List<String> images = new ArrayList<>();
        for (Element img : imagesDom) {
            images.add(img.attr("data-src"));
        }
        topic.setImg(images.size() > 0 ? images.get(0) : null);
        topic.setContent(content);
//        topic.setImages(images);
        topic.setUrl(url);
        topic.setTitle(title);
        topic.setDate(date);
        topic.setUser(user);
        return topic;
    }

    public static Content getTopicByUrl(String url, Content content) {
        Document doc = getDoc(url);
        if (null == doc) {
            return null;
        }
        Elements imagesDom = doc.select("#js_content img[data-src]");
        String contentTxt = doc.select("#js_content").first().html();
//        String date = doc.select("#post-date").first().text();
        String user = doc.select("#post-user").first().text();
        List<String> images = new ArrayList<>();
        for (Element img : imagesDom) {
            images.add(img.attr("data-src"));
        }
        if (StringUtils.isBlank(content.getImg()))
            content.setImg(images.size() > 0 ? images.get(0) : null);
        content.setContent(contentTxt);
        content.setContentMD5(MD5Util.md5(contentTxt));
        if (StringUtils.isBlank(content.getUser()))
            content.setUser(user);
        return content;
    }


    /**
     * 获取指定页的全部话题
     *
     * @param limit
     * @return
     */
    public static List<String> getTopicUrls(String url) {

        List<String> result = new ArrayList<String>();
        if (url.equals("") || null == url) {
            return result;
        }
        Document doc = getDoc(url);
        String jsonStr = doc.html().split("var msgList = ")[1].split("seajs.use")[0].trim();
        String[] tempList = jsonStr.split("content_url\":\"");
        for (int i = 0; i < tempList.length; i++) {
            if (tempList[i].startsWith("/s")) {
                result.add("http://mp.weixin.qq.com" + tempList[i].split("\",\"copyright_stat")[0].replaceAll("amp;", ""));
            }

        }
        return result;
    }

    //获取列表页所有文章
    public static Map<String, Content> getTopicUrlsMap(String url) {
        Map<String, Content> result = new HashMap<>();
        if (url.equals("") || null == url) {
            return result;
        }

        Document doc = getDoc(url);
        String jsonStr = null;
        try {
            //验证码会出现异常java.lang.ArrayIndexOutOfBoundsException
            jsonStr = doc.html().split("var msgList = ")[1].split("seajs.use")[0].trim();
        } catch (ArrayIndexOutOfBoundsException e) {
            // TODO: 2017/7/23 0023  破解验证码
            throw new WechatException(e);
        }
        if (!jsonStr.endsWith("}"))
            jsonStr = jsonStr.substring(0, jsonStr.lastIndexOf("}") + 1);
        JSONObject jsonObject = JSONObject.fromObject(jsonStr); //转为json对象
        if (jsonObject != null) {
            JSONArray jsonArray = jsonObject.getJSONArray("list");
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject app_msg_ext_info = jsonArray.getJSONObject(i);
                    if (app_msg_ext_info != null) {
                        JSONObject wechatInfo = app_msg_ext_info.getJSONObject("app_msg_ext_info");
                        if (wechatInfo == null) continue;
                        Content content = new Content();
                        content.setUser(wechatInfo.getString("author"));
                        String contentUrl = "http://mp.weixin.qq.com" + wechatInfo.getString("content_url").replaceAll("amp;", "");
                        content.setUrl(contentUrl);
                        content.setTitle(wechatInfo.getString("title"));
                        content.setImg(wechatInfo.getString("cover"));
                        content.setIntro(wechatInfo.getString("digest"));
                        //获取发布时间
                        JSONObject comm_msg_info = app_msg_ext_info.getJSONObject("comm_msg_info");
                        if (comm_msg_info != null) {
                            long dateTime = comm_msg_info.getLong("datetime") * 1000;
                            content.setDate(DateUtil.format(new Date(dateTime), "yyyy-MM-dd HH:mm:ss"));
                            content.setPt(DateUtil.format(new Date(dateTime), "yyyy-MM-dd"));
                        }
                        result.put(contentUrl, content);
                        JSONArray multi_app_msg_item_list = wechatInfo.getJSONArray("multi_app_msg_item_list");
                        if (multi_app_msg_item_list == null) continue;
                        for (int j = 0; j < multi_app_msg_item_list.size(); j++) {
                            JSONObject multiWechatInfo = multi_app_msg_item_list.getJSONObject(j);
                            if (multiWechatInfo == null) continue;
                            Content multiContent = new Content();
                            multiContent.setUser(multiWechatInfo.getString("author"));
                            String multiContentUrl = "http://mp.weixin.qq.com" + multiWechatInfo.getString("content_url").replaceAll("amp;", "");
                            multiContent.setUrl(multiContentUrl);
                            multiContent.setTitle(multiWechatInfo.getString("title"));
                            multiContent.setImg(multiWechatInfo.getString("cover"));
                            multiContent.setIntro(multiWechatInfo.getString("digest"));
                            multiContent.setDate(content.getDate());
                            multiContent.setPt(content.getPt());
                            result.put(multiContentUrl, multiContent);
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * 页面url路径 $("[name=em_weixinhao]:contains(letvwallpapers)").parents(".txt-box").find(".tit a").attr("href")
     */
    public String getListUrl() {
        String baseUrl = "http://weixin.sogou.com/weixin?type=1&ie=utf8&query=";
        String searchUrl = baseUrl + this.id;
        Document doc = getDoc(searchUrl);
        return doc.select("[name=em_weixinhao]:contains(" + this.id + ")").parents().select(".tit a").attr("href");
    }

    /**
     * 页面url路径 $("[name=em_weixinhao]:contains(letvwallpapers)").parents(".txt-box").find(".tit a").attr("href")
     */
    public static String getListUrl(String wechatId) {
        String baseUrl = "http://weixin.sogou.com/weixin?type=1&ie=utf8&query=";
        String searchUrl = baseUrl + wechatId;
        Document doc = getDoc(searchUrl);
        return doc.select("[name=em_weixinhao]:contains(" + wechatId + ")").parents().select(".tit a").attr("href");
    }

    /**
     * 获取指定页的文章doc对象
     *
     * @param page 当前页数
     * @return
     */
    public List<Document> getPageDocuments(int page) {
        String url = makeUrl(page);
        Document doc = getDoc(url);
        System.out.println(url);
        if (null == doc) {
            throw new WechatException("unknown error");
        }
        List<Document> docs = new ArrayList<Document>();
        if (0 != totalpages && page > totalpages) {
            return docs;
        }

        if (0 != totalpages) {
            getTotalPage(doc.select("pagesize").last().html().toString());
            if (page > totalpages) {
                return docs;
            }
        }

        ListIterator<Element> topicUrls = doc.select("url").listIterator();
        if (!topicUrls.hasNext()) {
            throw new WechatException(
                    "make sure the openId is right, otherwise no topics in this wechat account");
        }

        while (topicUrls.hasNext()) {
            Element topicUrl = topicUrls.next();
            topicUrl.select("title1").remove();
            Document topicDoc = getDoc(topicUrl.text());
            if (null != topicDoc) {
                docs.add(topicDoc);
            }
            topicDoc.attr("originUrl", url);
        }
        return docs;
    }

}

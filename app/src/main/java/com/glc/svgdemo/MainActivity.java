package com.glc.svgdemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.Resource;
import com.caverock.androidsvg.SVGImageView;
import com.caverock.androidsvg.SVGParseException;
import com.glc.svgdemo.databinding.ActivityMainBinding;
import com.glc.svgdemo.util.IOFormat;
import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParser;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.ExplainReasonCallbackWithBeforeParam;
import com.permissionx.guolindev.callback.ForwardToSettingsCallback;
import com.permissionx.guolindev.callback.RequestCallback;
import com.permissionx.guolindev.request.ExplainScope;
import com.permissionx.guolindev.request.ForwardScope;
import com.pixplicity.sharp.Sharp;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
* @Author: glc
* @Date: 2022/4/3
* @Time: 12:20
* @Description:
*/
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private DocumentBuilderFactory factory = null;
    private DocumentBuilder builder = null;
    private Document document = null;
    private InputStream inputStream = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //动态申请读写权限
        getPermission();



        //设置webView
        WebSettings webSettings=binding.wbViewOne.getSettings();
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUseWideViewPort(true);
        binding.wbViewOne.getSettings().setBuiltInZoomControls(true);// 会出现放大缩小的按钮
        binding.wbViewOne.getSettings().setSupportZoom(true);
        binding.wbViewOne.getSettings().setSupportMultipleWindows(true);
        binding.wbViewOne.setInitialScale(75);

        //加载assets中的svg
        binding.wbViewOne.loadUrl("file:///android_asset/bixin.svg");

        try {



            //首先找到assets中的文件
            factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
            inputStream=this.getResources().getAssets().open("bixin.svg");
            document = builder.parse(inputStream);
            //找到根Element
            Element root = document.getDocumentElement();
            //找到g标签
            NodeList nodes = root.getElementsByTagName("g");
            //找到第8个 g标签  ==口罩的标签
            Element item = (Element) nodes.item(8);
            //找到path标签
            NodeList pathTagName = item.getElementsByTagName("path");
            //找到第一个path标签
            Element item1 = (Element) pathTagName.item(0);
            String nodeName = item1.getNodeName();
            //改变属性颜色为 #ffffff 白色
            item1.setAttribute("fill","#ffffff");

            //将内存中的Document 写到手机存储文件
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.VERSION, "1");
            //将整个Document对象作为要写入xml文件的数据源
            DOMSource source = new DOMSource(document);
            String path = getExternalFilesDir(null) + "/newSvg.svg";
            //写入的目标文件
            StreamResult src = new StreamResult(new File(path));
            transformer.transform(source, src);




            //设置WebSettings
            WebSettings webSettingsTwo=binding.wbViewTwo.getSettings();
            webSettingsTwo.setLoadWithOverviewMode(true);
            webSettingsTwo.setJavaScriptEnabled(true);
            webSettingsTwo.setUseWideViewPort(true);
            //允许读取本地文件
            webSettingsTwo.setAllowFileAccess(true);
            binding.wbViewTwo.getSettings().setBuiltInZoomControls(true);// 会出现放大缩小的按钮
            binding.wbViewTwo.getSettings().setSupportZoom(true);
            binding.wbViewTwo.getSettings().setSupportMultipleWindows(true);
            binding.wbViewTwo.setInitialScale(75);
            //加载本地修改后保存的svg
            binding.wbViewTwo.loadUrl("file://"+ getExternalFilesDir(null) + "/newSvg.svg");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    //申请读写权限
    private void getPermission() {
        //权限读写权限申请
        PermissionX.init(this)
                .permissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .onExplainRequestReason(new ExplainReasonCallbackWithBeforeParam() {
                    @Override
                    public void onExplainReason(ExplainScope scope, List<String> deniedList, boolean beforeRequest) {
                        scope.showRequestReasonDialog(deniedList, "即将申请的权限是程序必须依赖的权限", "我已明白");
                    }
                }).onForwardToSettings(new ForwardToSettingsCallback() {
            @Override
            public void onForwardToSettings(ForwardScope scope, List<String> deniedList) {
                scope.showForwardToSettingsDialog(deniedList, "您需要去应用程序设置当中手动开启权限", "我已明白");
            }
        }).request(new RequestCallback() {
            @Override
            public void onResult(boolean allGranted, List<String> grantedList, List<String> deniedList) {
                if (allGranted) {
                    Toast.makeText(MainActivity.this, "所有申请的权限都已通过", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "您拒绝了如下权限：" + deniedList, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


}
package com.qhrking.render;

import com.jfinal.render.IMainRenderFactory;
import com.jfinal.render.Render;

/**
 * Created by qianhao on 2014/7/8.
 */
    public  final class VelocityLayoutRenderFactory implements IMainRenderFactory {

        public Render getRender(String view) {
            return new VelocityLayoutRender(view);
        }

        public String getViewExtension() {
            return ".html";
        }
    }

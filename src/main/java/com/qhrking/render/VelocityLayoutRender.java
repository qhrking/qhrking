package com.qhrking.render;


import com.jfinal.core.JFinal;
import com.jfinal.log.Logger;
import com.jfinal.render.Render;
import com.jfinal.render.RenderException;
import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.ToolManager;
import org.apache.velocity.tools.view.ViewToolContext;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

/**
 * @author qianhao
 *
 * from : http://phpd.cn/archives/795
 *
 */
public class VelocityLayoutRender extends Render {

    private static final long serialVersionUID = 1012573049421601960L;
    private transient static final String encoding = getEncoding();
    private transient static final String contentType  = "text/html;charset=" + encoding;
    /**
     * The velocity.properties key for specifying the servlet's error template.
     */
    public static final String PROPERTY_ERROR_TEMPLATE = "tools.view.servlet.error.template";

    /**
     * The velocity.properties key for specifying the relative directory holding
     * layout templates.
     */
    public static final String PROPERTY_LAYOUT_DIR = "tools.view.servlet.layout.directory";

    /**
     * The velocity.properties key for specifying the servlet's default layout
     * template's filename.
     */
    public static final String PROPERTY_DEFAULT_LAYOUT  = "tools.view.servlet.layout.default.template";

    /**
     * The default error template's filename.
     */
    public static final String DEFAULT_ERROR_TEMPLATE  = "error.vm";

    /**
     * The default layout directory
     */
    public static final String DEFAULT_LAYOUT_DIR = "/WEB-INF/layout/";

    /**
     * The default filename for the servlet's default layout
     */
    public static final String DEFAULT_DEFAULT_LAYOUT  = "default.vm";

    public static final String TOOLBOX_FILE  = "WEB-INF/classes/velocity-toolbox.xml";

    /**
     * The context key that will hold the content of the screen.
     * This key ($screen_content) must be present in the layout template for the
     * current screen to be rendered.
     */
    public static final String KEY_SCREEN_CONTENT = "screen_content";

    public static final String VM_CONTENT_DIR = "/WEB-INF/vm";

    /**
     * The context/parameter key used to specify an alternate layout to be used
     * for a request instead of the default layout.
     */
    public static final String KEY_LAYOUT  = "layout";

    /**
     * The context key that holds the {@link Throwable} that broke the rendering
     * of the requested screen.
     */
    public static final String KEY_ERROR_CAUSE  = "error_cause";

    /**
     * The context key that holds the stack trace of the error that broke the
     * rendering of the requested screen.
     */
    public static final String KEY_ERROR_STACKTRACE  = "stack_trace";

    /**
     * The context key that holds the {@link MethodInvocationException} that
     * broke the rendering of the requested screen.
     * If this value is placed in the context, then $error_cause will hold the
     * error that this invocation exception is wrapping.
     */
    public static final String KEY_ERROR_INVOCATION_EXCEPTION  = "invocation_exception";

    protected static String errorTemplate;
    protected static String layoutDir;
    protected static String defaultLayout;

    private transient static final Properties properties = new Properties();

    private transient static boolean notInit  = true;

    public VelocityLayoutRender(String view) {
        this.view = VM_CONTENT_DIR+view;
    }

    public static void setProperties(Properties properties) {
        Set<Entry<Object, Object>> set = properties.entrySet();
        for (Iterator<Entry<Object, Object>> it = set.iterator(); it.hasNext();) {
            Entry<Object, Object> e = it.next();
            VelocityLayoutRender.properties.put(e.getKey(), e.getValue());
        }
    }

    public void render() {
        init();
        PrintWriter writer = null;
        try {

            VelocityEngine velocityEngine = new VelocityEngine();
            ViewToolContext context = new ViewToolContext(velocityEngine, request, response, JFinal.me().getServletContext());

            ToolManager tm = new ToolManager();
            tm.setVelocityEngine(velocityEngine);
            tm.configure(JFinal.me().getServletContext().getRealPath(TOOLBOX_FILE));
            if (tm.getToolboxFactory().hasTools(Scope.REQUEST)) {
                context.addToolbox(tm.getToolboxFactory().createToolbox(Scope.REQUEST));
            }
            if (tm.getToolboxFactory().hasTools(Scope.APPLICATION)) {
                context.addToolbox(tm.getToolboxFactory().createToolbox(Scope.APPLICATION));
            }
            if (tm.getToolboxFactory().hasTools(Scope.SESSION)) {
                context.addToolbox(tm.getToolboxFactory().createToolbox(Scope.SESSION));
            }

            for (@SuppressWarnings("unchecked")
                Enumeration<String> attrs = request.getAttributeNames();
                attrs.hasMoreElements();
            ) {
                String attrName = attrs.nextElement();
                context.put(attrName, request.getAttribute(attrName));
            }

            Template template = Velocity.getTemplate(view);
            StringWriter sw = new StringWriter();
            template.merge(context, sw);
            context.put(KEY_SCREEN_CONTENT, sw.toString());

            response.setContentType(contentType);
            writer = response.getWriter();
            // BufferedWriter writer = new
            // BufferedWriter(new
            // OutputStreamWriter(System.out));
            Object obj = context.get(KEY_LAYOUT);
            String layout = (obj == null) ? null : obj.toString();
            if (layout == null) {
                // no alternate, use default
                layout = defaultLayout;
            } else {
                // make it a full(er) path
                layout = layoutDir + layout;
            }

            try {
                // load the layout template
                template = Velocity.getTemplate(layout);
            } catch (ResourceNotFoundException e) {
                Logger.getLogger(VelocityLayoutRender.class).error("Can't load layout \"" + layout + "\"", e);

                // if it was an alternate layout we couldn't get...
                if (!layout.equals(defaultLayout)) {
                    // try to get the default layout
                    // if this also fails, let the exception go
                    try {
                        template = Velocity.getTemplate(defaultLayout);
                    } catch (ResourceNotFoundException e2) {
                        Logger.getLogger(VelocityLayoutRender.class).error("Can't load layout \"" + defaultLayout + "\"", e2);
                    }
                }
            }

            template.merge(context, writer);
            writer.flush(); // flush and cleanup
        } catch (ResourceNotFoundException e) {
            throw new RenderException("Error : cannot find template " + view, e);
        } catch (ParseErrorException e) {
            throw new RenderException("Syntax error in template " + view + ":" + e, e);
        } catch (Exception e) {
            throw new RenderException(e);
        } finally {
            if (writer != null)
                writer.close();
        }
    }

    private void init() {
        if (notInit) {
            String webPath = JFinal.me().getServletContext().getRealPath("/");
            properties.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH, webPath);
            properties.setProperty(Velocity.ENCODING_DEFAULT, encoding);
            properties.setProperty(Velocity.INPUT_ENCODING, encoding);
            properties.setProperty(Velocity.OUTPUT_ENCODING, encoding);
            Velocity.init(properties);
            // setup layout
            errorTemplate = VelocityLayoutRender.properties.getProperty(PROPERTY_ERROR_TEMPLATE, DEFAULT_ERROR_TEMPLATE);
            layoutDir = VelocityLayoutRender.properties.getProperty(PROPERTY_LAYOUT_DIR, DEFAULT_LAYOUT_DIR);
            defaultLayout = VelocityLayoutRender.properties.getProperty(PROPERTY_DEFAULT_LAYOUT, DEFAULT_DEFAULT_LAYOUT);

            // preventive error checking! directory must end in /
            if (!layoutDir.endsWith("/")) {
                layoutDir += '/';
            }
            // for efficiency's sake, make defaultLayout a full path now
            defaultLayout = layoutDir + defaultLayout;

            // log the current settings
            Logger.getLogger(VelocityLayoutRender.class).info("VelocityRender: Error screen is '" + errorTemplate + "'");
            Logger.getLogger(VelocityLayoutRender.class).info("VelocityRender: Layout directory is '" + layoutDir + "'");
            Logger.getLogger(VelocityLayoutRender.class).info("VelocityRender: Default layout template is '" + defaultLayout + "'");
            notInit = false;
        }
    }

}
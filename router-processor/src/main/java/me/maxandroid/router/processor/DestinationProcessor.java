package me.maxandroid.router.processor;

import com.google.auto.service.AutoService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import me.maxandroid.router.annotations.Destination;

@AutoService(Processor.class)
public class DestinationProcessor extends AbstractProcessor {

    private static final String TAG = "DestinationProcessor";

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (roundEnvironment.processingOver()) {
            return false;
        }

        System.out.println(TAG + ">>> process start ...");

        String rootDir = processingEnv.getOptions().get("root_project_dir");

        Set<? extends Element> allDestinationElements =
                roundEnvironment.getElementsAnnotatedWith(Destination.class);
        System.out.println(TAG + ">>> all Destination elements count = " + allDestinationElements.size());

        if (allDestinationElements.size() < 1) {
            return false;
        }

        String className = "RouterMapping_" + System.currentTimeMillis();
        StringBuilder builder = new StringBuilder();
        builder.append("package me.maxandroid.router.mapping;\n\n");
        builder.append("import java.util.HashMap;\n");
        builder.append("import java.util.Map;\n\n");
        builder.append("public class ").append(className).append(" {\n\n");
        builder.append("    public static Map<String, String> get() {\n\n");
        builder.append("        Map<String, String> mapping = new HashMap<>();\n\n");

        final JsonArray destinationJsonArray = new JsonArray();

        for (Element element : allDestinationElements) {
            final TypeElement typeElement = ((TypeElement) element);
            final Destination destination = typeElement.getAnnotation(Destination.class);
            if (destination == null) {
                continue;
            }

            final String url = destination.url();
            final String description = destination.description();
            final String realPath = typeElement.getQualifiedName().toString();

            System.out.println(TAG + " >>> url = " + url);
            System.out.println(TAG + " >>> description = " + description);
            System.out.println(TAG + " >>> realPath = " + realPath);

            builder.append("        ")
                    .append("mapping.put(")
                    .append("\"" + url + "\"")
                    .append(", ")
                    .append("\"" + realPath + "\"")
                    .append(");\n");

            JsonObject item = new JsonObject();
            item.addProperty("url", url);
            item.addProperty("description", description);
            item.addProperty("realPath", realPath);

            destinationJsonArray.add(item);

        }
        builder.append("        return mapping;\n");
        builder.append("    }\n");
        builder.append("}\n");

        String mappingFullClassName = "me.maxandroid.router.mapping." + className;

        System.out.println(TAG + " >>> mappingFullClassName = "
                + mappingFullClassName);

        System.out.println(TAG + " >>> class content = \n" + builder);

        try {
            JavaFileObject source = processingEnv.getFiler()
                    .createSourceFile(mappingFullClassName);
            Writer writer = source.openWriter();
            writer.write(builder.toString());
            writer.flush();
            writer.close();
        } catch (Exception e) {
            throw new RuntimeException("Error while create file", e);
        }

        // 写入JSON到本地文件中

        // 检测父目录是否存在
        File rootDirFile = new File(rootDir);
        if (!rootDirFile.exists()) {
            throw new RuntimeException("root_project_dir not exist!");
        }

        // 创建 router_mapping 子目录
        File routerFileDir = new File(rootDirFile, "router_mapping");
        if (!routerFileDir.exists()) {
            routerFileDir.mkdir();
        }

        File mappingFile = new File(routerFileDir,
                "mapping_" + System.currentTimeMillis() + ".json");

        // 写入json内容
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(mappingFile));
            String jsonStr = destinationJsonArray.toString();
            out.write(jsonStr);
            out.flush();
            out.close();
        } catch (Throwable throwable) {
            throw new RuntimeException("Error while writing json", throwable);
        }

        System.out.println(TAG + " >>> process finish.");

        return false;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(
                Destination.class.getCanonicalName()
        );
    }
}

package ru.examples.svgExample;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Paths;

public class SvgToPng {

    public static void main(String[] args) {
        String svgFile = "picture/graph.svg";
        String pngFile = "temp/graph.png";

//        String svgFile = "picture/panda.svg";
//        String pngFile = "temp/panda.png";

        String svgUrl = null;
        try {
            svgUrl = Paths.get(svgFile).toUri().toURL().toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        TranscoderInput svgImage = new TranscoderInput(svgUrl);

        try (OutputStream outputStream = new FileOutputStream(pngFile)) {
            TranscoderOutput transcoderOutput = new TranscoderOutput(outputStream);
            PNGTranscoder pngTranscoder = new PNGTranscoder();
            pngTranscoder.transcode(svgImage, transcoderOutput);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TranscoderException e) {
            e.printStackTrace();
        }





/*
        JPEGTranscoder transcoder = new JPEGTranscoder();

        transcoder.addTranscodingHint(JPEGTranscoder.KEY_XML_PARSER_CLASSNAME,
                "org.apache.crimson.parser.XMLReaderImpl");
        transcoder.addTranscodingHint(JPEGTranscoder.KEY_QUALITY,
                new Float(1.0));

        TranscoderInput input = new TranscoderInput(new FileInputStream("rectangles.svg"));
        OutputStream ostream = new FileOutputStream("out.jpg");
        TranscoderOutput output = new TranscoderOutput(ostream);

        transcoder.transcode(input, output);
        ostream.close();
*/


/*
        String svg = "";

        Reader reader = new BufferedReader(new StringReader(svg));
        TranscoderInput svgImage = new TranscoderInput(reader);

        BufferedImageTranscoder transcoder = new BufferedImageTranscoder();
        transcoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, (float) component.getWidth());
        transcoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, (float) component.getHeight());
        try {
            transcoder.transcode(svgImage, null);
        } catch (TranscoderException e) {
            throw Throwables.propagate(e);
        }

//        return transcoder.getImage();
*/

    }
}

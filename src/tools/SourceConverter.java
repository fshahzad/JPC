package tools;


import java.io.*;
import java.util.*;
import java.util.regex.*;

public class SourceConverter
{

    public static void main(String[] args) throws IOException
    {
        String outputDir = ".";
        String outputPackage = "org.jpc.emulator.peripheral";
        String inputFile = "/home/ian/jpc/bochs/bochs-2.6.1/iodev/floppy.cc";
        String[] inputHeader = new String[] {"/home/ian/jpc/bochs/bochs-2.6.1/iodev/floppy.h", "floppy_include.txt"};
        for (int i=0; i < args.length; i++)
        {
            if (args[i].equals("-output"))
            {
                outputDir = args[i+1];
                i++;
            }
            else if (args[i].equals("-package"))
            {
                outputPackage = args[i+1];
                i++;
            }
            else if (args[i].equals("-input"))
            {
                inputFile = args[i+1];
                i++;
            }
        }
        File outRoot = new File(outputDir);
        File outDir = new File(outRoot, outputPackage.replaceAll("\\.", "/"));
        File inFile = new File(inputFile);
        String name = inFile.getName().replaceAll("\\.cc", "");
        File outFile = new File(outDir, name + ".java");
        StringBuilder b =new StringBuilder();
        for (String header: inputHeader)
        {
            BufferedReader r = new BufferedReader(new FileReader(header));
            String line;
            while ((line = r.readLine()) != null)
                b.append(line + "\n");
        }

        {
            BufferedReader r = new BufferedReader(new FileReader(inFile));
            String line;
            while ((line = r.readLine()) != null)
                b.append(line + "\n");
        }

        String result = convert(b.toString(), getRegex());
        BufferedWriter w = new BufferedWriter(new FileWriter(outFile));
        writeHeader(w, outputPackage, name);
        w.write(result.toString());
        writeFooter(w);
        w.flush();
        w.close();
    }

    private static void writeHeader(BufferedWriter w, String pack, String name) throws IOException  {
        w.append("package "+ pack+";\n\n");
        w.append("import org.jpc.support.*;\n");
        w.append("public class "+ name + "\n{\n");
        w.append("private static final boolean DEBUG = false;\n");
    }

    private static void writeFooter(BufferedWriter w) throws IOException {
        w.append("}");
    }

    private static List<Pair> getRegex() throws IOException
    {
        List<Pair> reg = new ArrayList();
        BufferedReader r = new BufferedReader(new FileReader("floppy_regex.txt"));
        String line;
        while ((line = r.readLine()) != null)
            reg.add(new Pair(line, r.readLine()));
        return reg;
    }

    private static String[] complex_types = new String[] {"floppy_t", "floppy_type_t"};

    private static String convert(String in, List<Pair> regex)
    {
        // simple regex
        for (Pair p: regex)
            in = in.replaceAll(p.key, p.value);

        // more complex replacements (single layer structs)
        for (String type: complex_types)
        {
            // definition
            Pattern def = Pattern.compile("typedef struct \\{([\\w\\s;/\\*]+)\\} "+type+";");
            Matcher matcher = def.matcher(in);
            if (matcher.find())
            {
                String body = matcher.group(1);
                String[] lines = body.trim().split("\n");
                for (int i=0; i < lines.length; i++)
                {
                    if (lines[i].length() == 0)
                        continue;
                    lines[i] = lines[i].substring(0, lines[i].indexOf(";")); // ignore comments after ;
                    lines[i] = lines[i].replaceAll("[\\s]+", " "); // contract spaces
                }

                String args = "";
                for (String arg: lines)
                {
                    if (arg.trim().length() == 0)
                        continue;
                    args += arg.trim() + ", ";
                }
                args = args.substring(0, args.length()-2);
                String constructorBody = "";
                for (String arg: lines)
                {
                    if (arg.trim().length() == 0)
                        continue;
                    String name = arg.trim().split(" ")[1];
                    constructorBody += "   this."+name + " = "+name+";\n";
                }
                in = in.replaceAll("typedef struct \\{([\\w\\s;/\\*]+)\\} "+type+";", "static class "+type+" {\n   public "+type+"("+args+")\n{\n"+constructorBody+"} $1}");
            }
            // uses

        }

        return in;
    }

    private static class Pair
    {
        String key, value;

        public Pair(String key, String value)
        {
            this.key= key;
            this.value = value;
        }
    }
}

import java.io.*;
import java.math.BigInteger;
import java.util.*;

public class ShamirSecretSolver {

    static class Point {
        BigInteger x;
        BigInteger y;

        Point(BigInteger x, BigInteger y) {
            this.x = x;
            this.y = y;
        }
    }

    // Decode value with specified base to BigInteger
    public static BigInteger decode(String value, int base) {
        return new BigInteger(value, base);
    }

    // Lagrange Interpolation to find constant term (c)
    public static BigInteger lagrangeInterpolation(List<Point> points) {
        BigInteger result = BigInteger.ZERO;

        for (int i = 0; i < points.size(); i++) {
            BigInteger xi = points.get(i).x;
            BigInteger yi = points.get(i).y;

            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;

            for (int j = 0; j < points.size(); j++) {
                if (i != j) {
                    BigInteger xj = points.get(j).x;
                    numerator = numerator.multiply(xj.negate());
                    denominator = denominator.multiply(xi.subtract(xj));
                }
            }

            BigInteger term = yi.multiply(numerator).divide(denominator);
            result = result.add(term);
        }

        return result;
    }

    // Manually parse the JSON-like file into a map
    public static Map<String, Map<String, String>> parseJsonFile(String filename) throws IOException {
        Map<String, Map<String, String>> data = new LinkedHashMap<>();
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;
        String currentKey = null;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.contains("keys")) {
                Map<String, String> keys = new HashMap<>();
                while (!(line = reader.readLine().trim()).equals("},")) {
                    String[] parts = line.replace("\"", "").replace(",", "").split(":");
                    keys.put(parts[0].trim(), parts[1].trim());
                }
                data.put("keys", keys);
            } else if (line.matches("\"\\d+\":.*")) {
                currentKey = line.split(":")[0].replaceAll("\"", "").trim();
                data.put(currentKey, new HashMap<>());
            } else if (line.contains("base") || line.contains("value")) {
                String[] parts = line.replace("\"", "").replace(",", "").split(":");
                data.get(currentKey).put(parts[0].trim(), parts[1].trim());
            }
        }
        reader.close();
        return data;
    }

    public static List<Point> extractPoints(Map<String, Map<String, String>> json) {
        List<Point> points = new ArrayList<>();
        Map<String, String> keys = json.get("keys");
        int k = Integer.parseInt(keys.get("k"));

        int count = 0;
        for (String key : json.keySet()) {
            if (key.equals("keys")) continue;
            Map<String, String> obj = json.get(key);
            int base = Integer.parseInt(obj.get("base"));
            String value = obj.get("value");
            BigInteger x = new BigInteger(key);
            BigInteger y = decode(value, base);
            points.add(new Point(x, y));
            count++;
            if (count >= k) break;
        }
        return points;
    }

    public static void main(String[] args) throws Exception {
        String[] files = {"testcase1.json", "testcase2.json"};

        for (int t = 0; t < files.length; t++) {
            Map<String, Map<String, String>> parsedJson = parseJsonFile(files[t]);
            List<Point> points = extractPoints(parsedJson);
            BigInteger secret = lagrangeInterpolation(points);
            System.out.println("Secret (constant term) for testcase " + (t + 1) + ": " + secret);
        }
    }
}

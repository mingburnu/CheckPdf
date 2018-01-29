import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.sql.*;
import java.util.Properties;

/**
 * Created by roderick on 2017/3/17.
 */
public class Check {
    public static void main(String[] args) {
        System.out.println("start test");

        //STEP 2: Register JDBC driver
        try {
            Properties prop = new Properties();
            InputStream input = Check.class.getClassLoader().getResourceAsStream("jdbc.properties");
            prop.load(input);

            //STEP 3: Open a connection
            System.out.println("Connecting to database...");
            Class.forName(prop.getProperty("driver"));
            Connection conn = DriverManager.getConnection(prop.getProperty("db"), prop.getProperty("user"), prop.getProperty("password"));

            //STEP 4: Execute a query
            System.out.println("Creating statement...");
            Statement stmt = conn.createStatement();
            String sql = prop.getProperty("sql");
            ResultSet rs = stmt.executeQuery(sql);

            File self = new File(Check.class.getProtectionDomain().getCodeSource().getLocation().getPath());
            String path = "\\error.log";
            File file = new File(self.getParentFile().getAbsolutePath() + path);

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            BufferedWriter bw = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
            String sepLine = "------------------------------------------------------------------------";

            //STEP 5: Extract data from result set
            while (rs.next()) {
                //Retrieve by column name
                String title = rs.getString("title");
                String fulltextUrl = rs.getString("fulltextUrl");

                URL url = new URL(fulltextUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(Integer.MAX_VALUE);
                int statusCode = connection.getResponseCode();

                if (statusCode != 200) {
                    //Display values
                    System.out.println("status: " + statusCode);
                    System.out.println("title: " + title);
                    System.out.println("url: " + fulltextUrl);
                    System.out.println(sepLine);

                    bw.write(Integer.toString(statusCode));
                    bw.newLine();
                    bw.write(title);
                    bw.newLine();
                    bw.write(fulltextUrl);
                    bw.newLine();
                    bw.write(sepLine);
                    bw.newLine();
                }

                connection.disconnect();
            }

            bw.close();

            System.out.println("finished!!");
            //STEP 6: Clean-up environment
            rs.close();
            stmt.close();
            conn.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

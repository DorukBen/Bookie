package com.karambit.bookie.helper;

import android.os.AsyncTask;

import com.orhanobut.logger.Logger;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * This class uploads file to server with progress value callback.
 *
 *  *No deprecated functions 2.09.2016
 *  *Uses HttpUrlConnection
 *
 *
 * Attention:
 *      - If you want to see progress softly change max buffer size but it affects performance
 *      for many devices buffer size need to be in( 8K < buffer size < 32K )spacing
 *
 *      -You need to give file name to constructor. Add file type after name : "test.jpg"
 *
 *      -Create unique file names otherwise output stream will overwrite the file in server
 *      which have same name
 *
 *      -If you want to reach progress state you need to implement UploadProgressChangedListener
 *
 * Example:
 *      UploadFileTask uP = new UploadFileTask(file_path, url, file_name);
 *
 *      **url must have "http://" protocol if contain
 *
 *      uP.execute(); will start to upload file
 *
 *      --If you want to use same object again create new constructor
 *      uP = new UploadFileTask(new_file_path, new_url, new_file_name);
 *
 *
 * Created by doruk on 2.09.2016.
 * Created for BookieApplication
 */
public class UploadFileTask extends AsyncTask<Void, Integer, HttpURLConnection> {

    public static final String TAG = UploadFileTask.class.getSimpleName();

    public static final String LINE_END = "\r\n";   //for server
    public static final String TWO_HYPHENS = "--"; //for server
    public static final String BOUNDARY =  "*****"; //indicator specifies start and end of byte array
    public static final int MAX_BUFFER_SIZE = 32000; //8K < buffer size < 32K

    private String mFileName;
    private String mFilePath;
    private String mURLString;

    private UploadProgressChangedListener mUploadProgressChangedListener;

    /**
     * @param filePath file path
     * @param URLString server url
     * @param fileName  file name with file type(test.jpg)
     */
    public UploadFileTask(String filePath , String URLString, String fileName) {
        mFilePath = filePath;
        mURLString = URLString;
        mFileName = fileName;
    }

    @Override
    protected void onPreExecute() {
        //start of the progress
        if (mUploadProgressChangedListener != null){
            mUploadProgressChangedListener.onProgressChanged(0);
        }
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
    }

    @Override
    protected HttpURLConnection doInBackground(Void... params) {
        try {
            String filePath = mFilePath;

            File srcFile = new File(filePath);
            FileInputStream fileInputStream = new FileInputStream(srcFile);

            URL url = new URL(mURLString);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Allow Inputs &amp; Outputs.
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);

            // Set HTTP method to POST.
            connection.setRequestMethod("POST");

            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + BOUNDARY);


            DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.writeBytes(TWO_HYPHENS + BOUNDARY + LINE_END);

            outputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + mFileName + "\"" + LINE_END);


            outputStream.writeBytes(LINE_END);

            int bytesAvailable = fileInputStream.available();
            int bufferSize = Math.min(bytesAvailable, MAX_BUFFER_SIZE);
            byte[] buffer = new byte[bufferSize];

            long transferedBytes = 0;
            long tatalByteCount = srcFile.length();

            // Read file from local memory
            int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                outputStream.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, MAX_BUFFER_SIZE);
                transferedBytes += bytesRead;
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                //Passes progress value to listener
                if (mUploadProgressChangedListener != null){
                    mUploadProgressChangedListener.onProgressChanged((int)(transferedBytes*100/tatalByteCount));
                }
            }

            outputStream.writeBytes(LINE_END);
            outputStream.writeBytes(TWO_HYPHENS + BOUNDARY + TWO_HYPHENS + LINE_END);

            // Responses from the server (code and message)
            int serverResponseCode = connection.getResponseCode();

            fileInputStream.close();
            outputStream.flush();
            outputStream.close();

            return connection;

        } catch (Exception ex) {
            Logger.e("Upload failed: " + ex.getMessage());

            return null;
        }
    }

    @Override
    protected void onPostExecute(HttpURLConnection connection) {
        super.onPostExecute(connection);

        if (mUploadProgressChangedListener != null) {

            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line + "\n");
                }
                bufferedReader.close();

                String response = stringBuilder.toString();

                switch (connection.getResponseCode()) {

                    case HttpURLConnection.HTTP_OK:
                        Logger.d("UploadFilesTask: OK!");

                        mUploadProgressChangedListener.onProgressCompleted(response);

                        break;// fine, go on

                    case HttpURLConnection.HTTP_GATEWAY_TIMEOUT:
                        Logger.e("UploadFilesTask: Gateway timeout");

                        mUploadProgressChangedListener.onProgressError();

                        break;// retry

                    case HttpURLConnection.HTTP_UNAVAILABLE:
                        Logger.e("UploadFilesTask: Unavaliable");

                        mUploadProgressChangedListener.onProgressError();

                        break;// retry, server is unstable

                    default:
                        Logger.e("UploadFilesTask: Unknown response code: " + connection.getResponseCode());
                        mUploadProgressChangedListener.onProgressError();

                        break; // abort
                }
            } catch (IOException e) {
                Logger.d("IOException caught while parsing response: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public interface UploadProgressChangedListener{
        void onProgressChanged(int progress);
        void onProgressCompleted(String response);
        void onProgressError();
    }

    public void setUploadProgressListener(UploadProgressChangedListener uploadProgressListener){
        mUploadProgressChangedListener = uploadProgressListener;
    }
}

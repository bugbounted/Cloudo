package com.promiseek.cloudo;

import android.content.Intent;

import android.os.Build;
import android.os.Bundle;

import android.os.Environment;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.promiseek.cloudo.Otp_Confirmation;
import com.promiseek.cloudo.R;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class MainActivity extends AppCompatActivity {
    public static TdApi.AuthorizationState authorizationState = null;
    public static Client client = null;
    public static final String newLine = System.getProperty("line.separator");
    public static volatile boolean haveAuthorization = false;
    public static final Lock authorizationLock = new ReentrantLock();
    public static final Condition gotAuthorization = authorizationLock.newCondition();
    public static volatile boolean needQuit = false;
    public static volatile boolean canQuit = false;
    public static final Client.ResultHandler defaultHandler = new DefaultHandler();
    public static String filePathName;
    public static String filePathDirectory;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        filePathDirectory = getFilesDir().getAbsolutePath();
        filePathName =getFilesDir().getAbsolutePath()+"/"+"tdlib.log";
        File file = new File(filePathName);

        // disable TDLib log
        Client.execute(new TdApi.SetLogVerbosityLevel(0));

        if (Client.execute(new TdApi.SetLogStream(new TdApi.LogStreamFile(filePathName, 1 << 27, false))) instanceof TdApi.Error) {
            throw new IOError(new IOException("Write access to the current directory is required"));
        }


        // create client
        client = Client.create(new UpdateHandler(), null, null);

        // test Client.execute
        defaultHandler.onResult(Client.execute(new TdApi.GetTextEntities("@telegram /test_command https://telegram.org telegram.me @gif @test")));

        // main loop
        while (!needQuit) {
            // await authorization
            authorizationLock.lock();
            try {
                while (!haveAuthorization) {
                    gotAuthorization.await();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                authorizationLock.unlock();
            }

            while (haveAuthorization) {
//                getCommand();
                Log.i("need authorization", "yes");
            }
        }
        while (!canQuit) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }



    }

    public static void onAuthorizationStateUpdated(TdApi.AuthorizationState authorizationState) {
        if (authorizationState != null) {
            MainActivity.authorizationState = authorizationState;
        }
        switch (MainActivity.authorizationState.getConstructor()) {
            case TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR:
                TdApi.TdlibParameters parameters = new TdApi.TdlibParameters();
                parameters.databaseDirectory = filePathDirectory;
                parameters.useMessageDatabase = true;
                parameters.useSecretChats = true;
                parameters.apiId = 2547332;
                parameters.apiHash = "3c5bae86e2e0bbc402559ec8b20ba0c0";
                parameters.systemLanguageCode = Locale.getDefault().getDisplayName();
                parameters.deviceModel = Build.MODEL;
                parameters.applicationVersion = "1.0";
                parameters.enableStorageOptimizer = true;

                client.send(new TdApi.SetTdlibParameters(parameters), new AuthorizationRequestHandler());
                Log.i("parameterDone", "yes");
                break;
            case TdApi.AuthorizationStateWaitEncryptionKey.CONSTRUCTOR:
                Log.i("need encryption key1", "yes");
                client.send(new TdApi.CheckDatabaseEncryptionKey(), new AuthorizationRequestHandler());
                Log.i("need encryption key2", "yes");
                break;
            case TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR: {
                Log.i("put phone number", "yes");
//                String phoneNumber = promptString("Please enter phone number: ");
//                client.send(new TdApi.SetAuthenticationPhoneNumber(phoneNumber, null), new AuthorizationRequestHandler());
                break;
            }
            case TdApi.AuthorizationStateWaitOtherDeviceConfirmation.CONSTRUCTOR: {
                String link = ((TdApi.AuthorizationStateWaitOtherDeviceConfirmation) MainActivity.authorizationState).link;
                System.out.println("Please confirm this login link on another device: " + link);
                break;
            }
            case TdApi.AuthorizationStateWaitCode.CONSTRUCTOR: {

//                String code = promptString("Please enter authentication code: ");
//                client.send(new TdApi.CheckAuthenticationCode(code), new AuthorizationRequestHandler());
                break;
            }
            case TdApi.AuthorizationStateWaitRegistration.CONSTRUCTOR: {
//                String firstName = promptString("Please enter your first name: ");
//                String lastName = promptString("Please enter your last name: ");
//                client.send(new TdApi.RegisterUser(firstName, lastName), new AuthorizationRequestHandler());
                break;
            }
            case TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR: {
//                String password = promptString("Please enter password: ");
//                client.send(new TdApi.CheckAuthenticationPassword(password), new AuthorizationRequestHandler());
                break;
            }
            case TdApi.AuthorizationStateReady.CONSTRUCTOR:
                    haveAuthorization = true;
                authorizationLock.lock();
                try {
                    gotAuthorization.signal();
                } finally {
                    authorizationLock.unlock();
                }
                break;
            case TdApi.AuthorizationStateLoggingOut.CONSTRUCTOR:
                haveAuthorization = false;
//                print("Logging out");
                break;
            case TdApi.AuthorizationStateClosing.CONSTRUCTOR:
                haveAuthorization = false;
//                print("Closing");
                break;
            case TdApi.AuthorizationStateClosed.CONSTRUCTOR:
//                print("Closed");
                if (!needQuit) {
                    client = Client.create(new UpdateHandler(), null, null); // recreate client after previous has closed
                } else {
                    canQuit = true;
                }
                break;
            default:
                System.err.println("Unsupported authorization state:" + newLine + MainActivity.authorizationState);
        }
    }

    private static class DefaultHandler implements Client.ResultHandler {
        @Override
        public void onResult(TdApi.Object object) {
//            print(object.toString());
        }
    }

    public static class AuthorizationRequestHandler implements Client.ResultHandler {
        @Override
        public void onResult(TdApi.Object object) {
            switch (object.getConstructor()) {
                case TdApi.Error.CONSTRUCTOR:
                    System.err.println("Receive an error:" + newLine + object);
                    onAuthorizationStateUpdated(null); // repeat last action
                    break;
                case TdApi.Ok.CONSTRUCTOR:
                    // result is already received through UpdateAuthorizationState, nothing to do
                    break;
                default:
                    System.err.println("Receive wrong response from TDLib:" + newLine + object);
            }
        }
    }

}



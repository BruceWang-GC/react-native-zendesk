package io.dcvz.rnzendesk;

import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;

import zendesk.configurations.Configuration;
import zendesk.core.Zendesk;
import zendesk.core.Identity;
import zendesk.core.JwtIdentity;
import zendesk.core.AnonymousIdentity;
import zendesk.support.Support;
import zendesk.support.guide.HelpCenterActivity;
import zendesk.support.guide.ViewArticleActivity;
import zendesk.support.request.RequestActivity;
import zendesk.support.requestlist.RequestListActivity;

import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;

import java.util.ArrayList;

public class RNZendeskBridge extends ReactContextBaseJavaModule {


    public RNZendeskBridge(ReactApplicationContext reactContext) {
        super(reactContext);
    }


    @Override
    public String getName() {
        return "RNZendesk";
    }

    // MARK: - Initialization

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @ReactMethod
    public void initialize(ReadableMap config) {
        String appId = config.getString("appId");
        String zendeskUrl = config.getString("zendeskUrl");
        String clientId = config.getString("clientId");


        Zendesk.INSTANCE.init(getReactApplicationContext(), zendeskUrl, appId, clientId);
        // 默认为匿名认证
        Identity identity = new AnonymousIdentity();
        Zendesk.INSTANCE.setIdentity(identity);

        Support.INSTANCE.init(Zendesk.INSTANCE);
    }

    // MARK: - Indentification

    @ReactMethod
    public void identifyJWT(String token) {
        JwtIdentity identity = new JwtIdentity(token);
        Zendesk.INSTANCE.setIdentity(identity);
    }

   @ReactMethod
   public void identifyAnonymous(String name, String email) {
       Identity identity = new AnonymousIdentity.Builder()
           .withNameIdentifier(name)
           .withEmailIdentifier(email)
           .build();

       Zendesk.INSTANCE.setIdentity(identity);
   }


    // MARK: - UI Methods

    @ReactMethod
    public void showHelpCenter(ReadableMap options) {

        try {
            Configuration helpConfig = HelpCenterActivity.builder()
                    .withContactUsButtonVisible(!(options.hasKey("hideContactSupport") && options.getBoolean("hideContactSupport")))
                    .withShowConversationsMenuButton(false)
                    .config();

            Configuration articleConfig = ViewArticleActivity.builder()
                    .withContactUsButtonVisible(false)
                    .config();

            Configuration requestConfig = RequestActivity.builder()
                    .withTags("android", "mobile")
                    .config();

            Intent intent = HelpCenterActivity.builder()
                    .intent(getReactApplicationContext(), helpConfig, articleConfig, requestConfig);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (intent != null) {
                getReactApplicationContext().startActivity(intent);
            }

        }  catch (Exception e) {
            System.out.println("E_FAILED_TO_SHOW_ZENDESK");
            System.out.println(e);
        }

    }
    
    @ReactMethod
    public void showNewTicket(ReadableMap options) {
        ArrayList tags = options.getArray("tags").toArrayList();

        Intent intent = RequestActivity.builder()
                .withTags(tags)
                .intent(getReactApplicationContext());

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        getReactApplicationContext().startActivity(intent);
    }

    @ReactMethod
    public void showTicketList() {
        RequestListActivity.builder()
            .show(getReactApplicationContext());
    }
}
/*
 * Copyright (c) 2016. Jahir Fiquitiva. Android Developer. All rights reserved.
 */

package jahirfiquitiva.iconshowcase.models;

import java.util.ArrayList;

public class RequestList {

    private static ArrayList<RequestItem> appsToRequest;

    public RequestList(ArrayList<RequestItem> appsToRequest) {
        RequestList.appsToRequest = appsToRequest;
    }

    public static void setRequestList(ArrayList<RequestItem> appsToRequest) {
        RequestList.appsToRequest = appsToRequest;
    }

    public static ArrayList<RequestItem> getRequestList() {
        return RequestList.appsToRequest != null ?
                RequestList.appsToRequest.size() > 0 ? RequestList.appsToRequest : null
                : null;
    }

}
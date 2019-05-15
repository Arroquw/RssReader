package com.example.myfragmentapp.screens;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.navigation.fragment.NavHostFragment;

import com.example.myfragmentapp.R;
import com.example.myfragmentapp.adapters.RssItemAdapter;
import com.example.myfragmentapp.models.RssData;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;


public class RssFragment extends Fragment {
    private ArrayList<RssData> listData;
    private Context mContext;
    private RssItemAdapter itemAdapter;
    //

    public RssFragment(){}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.rss_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        assert getArguments() != null;
        String rssString = RssFragmentArgs.fromBundle(getArguments()).getFeedLink();
        listData = new ArrayList<>();
        RssDataController controller = new RssDataController();
        controller.execute(rssString);
        ListView listView = Objects.requireNonNull(getView()).findViewById(R.id.rss_list_view);
        itemAdapter = new RssItemAdapter(mContext, R.layout.rss_item, listData);
        listView.setAdapter(itemAdapter);
        listView.setOnItemClickListener(onItemClickListener);
    }

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                long arg3) {
            // TODO Auto-generated method
            RssData data;
            data = listData.get(arg2);
            RssFragmentDirections.ActionRssFragmentToRssViewFragment action;
//            Bundle postInfo = new Bundle();
            if(data.rssContent == null) {
                action = RssFragmentDirections.actionRssFragmentToRssViewFragment(data.rssLink);
            } else {
              action = RssFragmentDirections.actionRssFragmentToRssViewFragment(data.rssContent);
            }
            NavHostFragment.findNavController(RssFragment.this).navigate(action);
        }
    };

    private enum RSSXMLTag {
        TITLE, DATE, LINK, CONTENT, GUID, IGNORETAG;
    }

    private class RssDataController extends AsyncTask<String, Integer, ArrayList<RssData>> {
        private RSSXMLTag currentTag;

        @Override
        protected ArrayList<RssData> doInBackground(String... params) {
            // TODO Auto-generated method stub
            String urlStr = params[0];
            InputStream is = null;
            ArrayList<RssData> rssDataList = new ArrayList<RssData>();
            try {
                URL url = new URL(urlStr);
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                connection.setReadTimeout(10 * 1000);
                connection.setConnectTimeout(10 * 1000);
                connection.setRequestMethod("GET");
                connection.setDoInput(true);
                connection.connect();
                int response = connection.getResponseCode();
                Log.d("debug", "The response is: " + response);
                is = connection.getInputStream();

                // parse xml after getting the data
                XmlPullParserFactory factory = XmlPullParserFactory
                        .newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(is, null);

                int eventType = xpp.getEventType();
                RssData pdData = null;
                SimpleDateFormat dateFormat = new SimpleDateFormat(
                        "EEE, DD MMM yyyy HH:mm:ss", Locale.getDefault());
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_DOCUMENT) {

                    } else if (eventType == XmlPullParser.START_TAG) {
                        switch (xpp.getName()) {
                            case "item":
                                pdData = new RssData();
                                currentTag = RSSXMLTag.IGNORETAG;
                                break;
                            case "title":
                                currentTag = RSSXMLTag.TITLE;
                                break;
                            case "link":
                                currentTag = RSSXMLTag.LINK;
                                break;
                            case "pubDate":
                                currentTag = RSSXMLTag.DATE;
                                break;
                            case "content":
                                currentTag = RSSXMLTag.CONTENT;
                                break;
                        }
                    } else if (eventType == XmlPullParser.END_TAG) {
                        if (xpp.getName().equals("item")) {
                            // format the data here, otherwise format data in
                            // Adapter
                            assert pdData != null;
                            Date postDate = dateFormat.parse(pdData.rssDate);
                            pdData.rssDate = dateFormat.format(postDate);
                            rssDataList.add(pdData);
                        } else {
                            currentTag = RSSXMLTag.IGNORETAG;
                        }
                    } else if (eventType == XmlPullParser.TEXT) {
                        String content = xpp.getText();
                        content = content.trim();
                        Log.d("debug", content);
                        if (pdData != null) {
                            switch (currentTag) {
                                case TITLE:
                                    if (content.length() != 0) {
                                        if (pdData.rssTitle != null) {
                                            pdData.rssTitle += content;
                                        } else {
                                            pdData.rssTitle = content;
                                        }
                                    }
                                    break;
                                case LINK:
                                    if (content.length() != 0) {
                                        if (pdData.rssLink != null) {
                                            pdData.rssLink += content;
                                        } else {
                                            pdData.rssLink = content;
                                        }
                                    }
                                    break;
                                case DATE:
                                    if (content.length() != 0) {
                                        if (pdData.rssDate != null) {
                                            pdData.rssDate += content;
                                        } else {
                                            pdData.rssDate = content;
                                        }
                                    }
                                    break;
                                case CONTENT:
                                    if (content.length() != 0) {
                                        if (pdData.rssContent != null) {
                                            pdData.rssContent += content;
                                        } else {
                                            pdData.rssContent = content;
                                        }
                                    }
                                default:
                                    break;
                            }
                        }
                    }

                    eventType = xpp.next();
                }
                Log.v("tst", String.valueOf((rssDataList.size())));
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return rssDataList;
        }

        @Override
        protected void onPostExecute(ArrayList<RssData> result) {
            // TODO Auto-generated method stub
            for (int i = 0; i < result.size(); i++) {
                listData.add(result.get(i));
            }

            itemAdapter.notifyDataSetChanged();
        }
    }
}
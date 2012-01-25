package com.home.lepradroid.tasks;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.text.TextUtils;
import android.util.Pair;

import com.home.lepradroid.R;
import com.home.lepradroid.commons.Commons;
import com.home.lepradroid.commons.Commons.PostSourceType;
import com.home.lepradroid.interfaces.PostsUpdateListener;
import com.home.lepradroid.interfaces.UpdateListener;
import com.home.lepradroid.listenersworker.ListenersWorker;
import com.home.lepradroid.objects.Post;
import com.home.lepradroid.serverworker.ServerWorker;
import com.home.lepradroid.utils.Logger;
import com.home.lepradroid.utils.Utils;

public class GetPostsTask extends BaseTask
{
    private PostSourceType type;
    private LoadImagesTask loadImagesTask;
    
    static final Class<?>[] argsClasses = new Class[2];
    static Method methodOnPostsUpdate;
    static 
    {
        try
        {
        	argsClasses[0] = PostSourceType.class;
        	argsClasses[1] = boolean.class;
            methodOnPostsUpdate = PostsUpdateListener.class.getMethod("OnPostsUpdate", argsClasses);    
        }
        catch (Throwable t) 
        {           
            Logger.e(t);
        }        
    }
    
    @Override
	public void finish()
    {
    	if(loadImagesTask != null) loadImagesTask.finish();
    	super.finish();
    }
    
    public GetPostsTask(PostSourceType type)
    {
        this.type = type;
    }
    
    @SuppressWarnings("unchecked")
    public void notifyAboutPostsUpdate()
    {
        final List<PostsUpdateListener> listeners = ListenersWorker.Instance().getListeners(PostsUpdateListener.class);
        final Object args[] = new Object[2];
        args[0] = type;
        args[1] = true; // TODO add new 'load new posts'
        
        
        for(PostsUpdateListener listener : listeners)
        {
            publishProgress(new Pair<UpdateListener, Pair<Method, Object[]>>(listener, new Pair<Method, Object[]> (methodOnPostsUpdate, args)));
        }
    }

    @Override
    protected Throwable doInBackground(Void... params)
    {
        try
        {
            ServerWorker.Instance().clearPostsByType(type);
            
            final String html = ServerWorker.Instance().getContent(type == PostSourceType.MAIN ? Commons.SITE_URL : Commons.MY_STUFF_URL).replace("&#150;", "-").replace("&#151;", "-"); // TODO problem with parsing 
            final Document document = Jsoup.parse(html);
            final Element content = document.getElementById("content");
            final Elements posts = content.getElementsByClass("dt");
            for (@SuppressWarnings("rawtypes")
            Iterator iterator = posts.iterator(); iterator.hasNext();)
            {
                Element element = (Element) iterator.next();
                String text = element.text();
                Post post = new Post(type);
                post.Text = TextUtils.isEmpty(text) ? "..." : text;
                post.Html = element.html();
                
                Elements images = element.getElementsByTag("img");
                if(!images.isEmpty())
                {
                    post.ImageUrl = images.first().attr("src");
                    
                    for (Element image : images)
                    {
                        String width = image.attr("width");
                        if(!TextUtils.isEmpty(width))
                            post.Html = post.Html.replace("width=\"" + width + "\"", "");
                        
                        String height = image.attr("height");
                        if(!TextUtils.isEmpty(height))
                            post.Html = post.Html.replace("height=\"" + height + "\"", "");
                        
                        post.Html = post.Html.replace(image.attr("src"), "http://src.sencha.io/303/303/" + image.attr("src"));
                    }
                }
                
                Element authorParent = element.parent();
                if(authorParent != null)
                {
                	Elements rating = authorParent.getElementsByTag("em");
                    post.Rating = Integer.valueOf(rating.first().text());
                    
                    Elements author = authorParent.getElementsByClass("p");
                    if(!author.isEmpty())
                    {
                        Elements span = author.first().getElementsByTag("span");
                        Elements a = span.first().getElementsByTag("a");
                        if(a.size() == 2)
                            post.Comments = a.get(0).text() + " / " + "<b>" + a.get(1).text() + "</b>";
                        else
                            post.Comments = Utils.getString(R.string.No_Comments);
                        
                        post.Author = author.first().getElementsByTag("a").first().text();
                        post.Signature = author.first().text().split("\\|")[0].replace(post.Author, "<b>" + post.Author + "</b>");
                    }
                }
                
                ServerWorker.Instance().addNewPost(post);
            }
            
            loadImagesTask = new LoadImagesTask(type);
            loadImagesTask.execute();
        }
        catch (Throwable t)
        {
            setException(t);
        }
        finally
        {
            notifyAboutPostsUpdate();
        }
        
        return e;
    }
}

package com.home.lepradroid;

import java.util.ArrayList;

import com.home.lepradroid.objects.Post;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

class PostsAdapter extends ArrayAdapter<Post>
{
    private ArrayList<Post> posts = new ArrayList<Post>();
            
    public PostsAdapter(Context context, int textViewResourceId,
            ArrayList<Post> posts)
    {
        super(context, textViewResourceId, posts);
        this.posts = posts;
    }

    public int getCount() 
    {
        return posts.size();
    }
    
    public Post getItem(int position) 
    {
        return posts.get(position);
    }
    
    public long getItemId(int position) 
    { 
        return position;
    }
    
    public void updateContent(ArrayList<Post> newPosts)
    {
        this.posts.clear();
        this.posts.addAll(newPosts);
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) 
    {
        final Post post = getItem(position);
        
        LayoutInflater aInflater=LayoutInflater.from(getContext());

        View view = aInflater.inflate(R.layout.post_row_view, parent, false);
        
        TextView text = (TextView)view.findViewById(R.id.text);
        text.setText(post.Text);
        
        TextView author = (TextView)view.findViewById(R.id.author);
        author.setText(Html.fromHtml(post.Signature));
        
        TextView comments = (TextView)view.findViewById(R.id.comments);
        comments.setText(Html.fromHtml(post.Comments));
        
        TextView rating = (TextView)view.findViewById(R.id.rating);
        rating.setText(post.Rating.toString());
        
        ImageView image = (ImageView)view.findViewById(R.id.image);
        
        if(!TextUtils.isEmpty(post.ImageUrl))
        {
            image.setVisibility(View.VISIBLE);
            
            if(post.dw != null)
            {
                image.setImageDrawable(post.dw);
            }
        }

        return view;
    }
}
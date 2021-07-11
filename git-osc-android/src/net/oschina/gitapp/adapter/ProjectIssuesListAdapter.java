package net.oschina.gitapp.adapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import net.oschina.gitapp.R;
import net.oschina.gitapp.bean.Commit;
import net.oschina.gitapp.bean.Issue;
import net.oschina.gitapp.bean.Project;
import net.oschina.gitapp.bean.URLs;
import net.oschina.gitapp.common.BitmapManager;
import net.oschina.gitapp.common.StringUtils;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 项目Issues列表适配器
 * @created 2014-05-28 上午11：19
 * @author 火蚁（http://my.oschina.net/LittleDY）
 * 
 * 最后更新：
 * 更新者：
 */
public class ProjectIssuesListAdapter extends MyBaseAdapter<Issue> {
	
	private BitmapManager bmpManager;
	
	static class ListItemView {
		public ImageView face;//用户头像
		public TextView title;
		public TextView username;
		public TextView date;
		public TextView comment_count;// 评论数量
	}
	
	public ProjectIssuesListAdapter(Context context, List<Issue> data, int resource) {
		super(context, data, resource);
		this.bmpManager = new BitmapManager(BitmapFactory.decodeResource(
				context.getResources(), R.drawable.mini_avatar));
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ListItemView  listItemView = null;
		if (convertView == null) {
			//获取list_item布局文件的视图
			convertView = listContainer.inflate(this.itemViewResource, null);
			
			listItemView = new ListItemView();
			
			//获取控件对象
			listItemView.face = (ImageView) convertView.findViewById(R.id.projectissues_listitem_userface);
			listItemView.title = (TextView) convertView.findViewById(R.id.projectissues_listitem_title);
			listItemView.username = (TextView) convertView.findViewById(R.id.projectissues_listitem_author);
			listItemView.date = (TextView) convertView.findViewById(R.id.projectissues_listitem_date);
			listItemView.comment_count = (TextView) convertView.findViewById(R.id.projectissues_listitem_count);
			
			//设置控件集到convertView
			convertView.setTag(listItemView);
		}else {
			listItemView = (ListItemView)convertView.getTag();
		}
		
		Issue issue = getItem(position);
		
		// 1.加载项目作者头像
		String portrait = issue.getAuthor() == null || issue.getAuthor().getPortrait() == null ? "" : issue.getAuthor().getPortrait();
		if (portrait.endsWith("portrait.gif") || StringUtils.isEmpty(portrait)) {
			listItemView.face.setImageResource(R.drawable.mini_avatar);
		} else {
			String portraitURL = URLs.HTTP + URLs.HOST + URLs.URL_SPLITTER + issue.getAuthor().getPortrait();
			bmpManager.loadBitmap(portraitURL, listItemView.face);
		}
		/*
		if (faceClickEnable) {
			listItemView.face.setOnClickListener(faceClickListener);
		}*/
		
		// 2.显示相关信息
		listItemView.title.setText(issue.getTitle());
		listItemView.username.setText(issue.getAuthor() == null ? "" : issue.getAuthor().getName());
		SimpleDateFormat f = new  SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date time = issue.getCreatedAt();
		listItemView.date.setText(StringUtils.friendly_time(f.format(time)));
		
		return convertView;
	}
}

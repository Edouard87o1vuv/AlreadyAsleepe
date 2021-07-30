package net.oschina.gitapp.ui.fragments;

import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import net.oschina.gitapp.R;
import net.oschina.gitapp.adapter.ViewPageFragmentAdapter;
import net.oschina.gitapp.bean.Project;
import net.oschina.gitapp.common.Contanst;
import net.oschina.gitapp.common.UIHelper;
import net.oschina.gitapp.ui.basefragment.BaseViewPagerFragment;

public class ProjectViewPageFragment extends BaseViewPagerFragment {
	
	private final int MENU_MORE_ID = 0;
	private final int MENU_CREATE_ID = 1;
	
	private Project mProject;
	
	public static ProjectViewPageFragment newInstance(Project project) {
		ProjectViewPageFragment fragment = new ProjectViewPageFragment();
		Bundle args = new Bundle();
		args.putSerializable(Contanst.PROJECT, project);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	protected void onSetupTabAdapter(ViewPageFragmentAdapter adapter) {
		Bundle args = getArguments();
		if (args != null) {
			mProject = (Project) args.getSerializable(Contanst.PROJECT);
		}
		String[] title = getResources().getStringArray(R.array.project_title_array);
		
		adapter.addTab(title[0], "project_readme", ProjectReadMeFragment.class, args);
		adapter.addTab(title[1], "project_commitlist", ProjectCommitListFragment.class, args);
		adapter.addTab(title[2], "project_codetree", ProjectCodeTreeFragment.class, args);
		
		// 是否可以pr
		if (mProject.isPullRequestsEnabled()) {
			//adapter.addTab(title[2], "project_pullrequest", ProjectCodeTreeFragment.class, args);
		}
		
		// 是否可以接受issue
		if (mProject.isIssuesEnabled()) {
			adapter.addTab(title[3], "project_issuelist", ProjectIssuesListFragment.class, args);
		}
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		MenuItem moreOption = menu.add(0, MENU_MORE_ID, MENU_MORE_ID, "更多");
		moreOption.setIcon(R.drawable.abc_ic_menu_moreoverflow_normal_holo_dark);
		MenuItemCompat.setShowAsAction(moreOption,
				MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();
		switch (id) {
		case MENU_CREATE_ID:
			// 新增issue
			UIHelper.showIssueEditOrCreate(getGitApplication(), mProject, null);
			break;
		case MENU_MORE_ID:
			UIHelper.ToastMessage(getGitApplication(), "更多");
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}

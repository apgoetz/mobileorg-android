package com.matburt.mobileorg.Gui.Outline;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.Checkable;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.matburt.mobileorg.R;
import com.matburt.mobileorg.Gui.Theme.DefaultTheme;
import com.matburt.mobileorg.OrgData.OrgFileParser;
import com.matburt.mobileorg.OrgData.OrgNode;
import com.matburt.mobileorg.OrgData.OrgProviderUtils;
import com.matburt.mobileorg.util.OrgUtils;

public class OutlineItem extends RelativeLayout implements Checkable {
		
	private TextView titleView;
	private TextView tagsView;
	private boolean levelFormatting = true;
	
	public OutlineItem(Context context) {
		super(context);
		View.inflate(getContext(), R.layout.outline_item, this);
		titleView = (TextView) findViewById(R.id.outline_item_title);
		tagsView = (TextView) findViewById(R.id.outline_item_tags);
		
		int fontSize = OrgUtils.getFontSize(getContext());
		tagsView.setTextSize(fontSize);
	}
	
	public void setLevelFormating(boolean enabled) {
		this.levelFormatting = enabled;
	}
	
	public void setup(OrgNode node, boolean expanded, DefaultTheme theme, ContentResolver resolver) {
		setupTags(node.tags, node.tags_inherited, theme);
		
		SpannableStringBuilder titleSpan = new SpannableStringBuilder(node.name);
		
		if(node.name.startsWith(OrgFileParser.BLOCK_SEPARATOR_PREFIX)) {
			setupAgendaBlock(titleSpan, theme);
			return;
		}
		
		if (levelFormatting)
			applyLevelFormating(theme, node.level, titleSpan);
		setupTitle(node.name, theme, titleSpan);
		setupPriority(node.priority, theme, titleSpan);
		setupTodo(node.todo, titleSpan, theme, resolver);
		
		if (levelFormatting)
			applyLevelIndentation(node.level, titleSpan);
		
		if(expanded == false)
			setupChildrenIndicator(node, resolver, theme, titleSpan);
				
		titleSpan.setSpan(new StyleSpan(Typeface.NORMAL), 0, titleSpan.length(), 0);
		titleView.setText(titleSpan);
	}
	
	public void setupChildrenIndicator(OrgNode node,
			ContentResolver resolver, DefaultTheme theme,
			SpannableStringBuilder titleSpan) {
		if (node.hasChildren(resolver)) {
			titleSpan.append("...");
			titleSpan.setSpan(new ForegroundColorSpan(theme.defaultForeground),
					titleSpan.length() - "...".length(), titleSpan.length(), 0);
		}
	}

	public static void setupTodo(String todo, SpannableStringBuilder titleSpan, DefaultTheme theme, ContentResolver resolver) {
		if(TextUtils.isEmpty(todo) == false) {
			Spannable todoSpan = new SpannableString(todo + " ");
			
			boolean active = OrgProviderUtils.isTodoActive(todo, resolver);
			
			todoSpan.setSpan(new ForegroundColorSpan(active ? theme.c1Red : theme.caLGreen), 0,
					todo.length(), 0);
			titleSpan.insert(0, todoSpan);
		}
	}
	
	public static void setupPriority(String priority, DefaultTheme theme, SpannableStringBuilder titleSpan) {
		if (priority != null && TextUtils.isEmpty(priority) == false) {
			Spannable prioritySpan = new SpannableString(priority + " ");
			prioritySpan.setSpan(new ForegroundColorSpan(theme.c3Yellow), 0,
					priority.length(), 0);
			titleSpan.insert(0, prioritySpan);
		}
	}
	
	public static void applyLevelIndentation(long level, SpannableStringBuilder item) {
		for(int i = 0; i < level; i++)
			item.insert(0, "   ");
	}
	
	public static void applyLevelFormating(DefaultTheme theme, long level, SpannableStringBuilder item) {
		item.setSpan(
				new ForegroundColorSpan(theme.levelColors[(int) Math
						.abs((level) % theme.levelColors.length)]), 0, item
						.length(), 0);
	}
	
	public void setupTitle(String name, DefaultTheme theme, SpannableStringBuilder titleSpan) {
		titleView.setGravity(Gravity.LEFT);
		titleView.setTextSize(OrgUtils.getFontSize(getContext()));

		if (name.startsWith("COMMENT"))
			titleSpan.setSpan(new ForegroundColorSpan(theme.gray), 0,
					"COMMENT".length(), 0);
		else if (name.equals("Archive"))
			titleSpan.setSpan(new ForegroundColorSpan(theme.gray), 0,
					"Archive".length(), 0);
		
		formatLinks(theme, titleSpan);
	}
	
	public void setupAgendaBlock(SpannableStringBuilder titleSpan, DefaultTheme theme) {
		titleSpan.delete(0, OrgFileParser.BLOCK_SEPARATOR_PREFIX.length());

		titleSpan.setSpan(new ForegroundColorSpan(theme.defaultForeground), 0,
				titleSpan.length(), 0);
		titleSpan.setSpan(new StyleSpan(Typeface.BOLD), 0,
				titleSpan.length(), 0);

		titleView.setTextSize(OrgUtils.getFontSize(getContext()) + 4);
		//titleView.setBackgroundColor(theme.c4Blue);
		titleView.setGravity(Gravity.CENTER_VERTICAL
				| Gravity.CENTER_HORIZONTAL);

		titleView.setText(titleSpan);
	}

	public static final Pattern urlPattern = Pattern.compile("\\[\\[[^\\]]*\\]\\[([^\\]]*)\\]\\]");
	private static void formatLinks(DefaultTheme theme, SpannableStringBuilder titleSpan) {
		Matcher matcher = urlPattern.matcher(titleSpan);
		while(matcher.find()) {
			titleSpan.delete(matcher.start(), matcher.end());
			titleSpan.insert(matcher.start(), matcher.group(1));
		
			titleSpan.setSpan(new ForegroundColorSpan(theme.c4Blue),
					matcher.start(), matcher.start() + matcher.group(1).length(), 0);	
			
			matcher = urlPattern.matcher(titleSpan);
		}
	}
	
	public void setupTags(String tags, String tagsInherited, DefaultTheme theme) {
		if(TextUtils.isEmpty(tags) == false || TextUtils.isEmpty(tagsInherited) == false) {
			if (TextUtils.isEmpty(tagsInherited) == false)
				tagsView.setText(tags + "::" + tagsInherited);
			else
				tagsView.setText(tags);

			tagsView.setTextColor(theme.gray);
			tagsView.setVisibility(View.VISIBLE);
		} else
			tagsView.setVisibility(View.GONE);
	}

	@Override
	public boolean isChecked() {
		return false;
	}

	@Override
	public void setChecked(boolean checked) {
		if(checked)
			setBackgroundResource(R.drawable.outline_item_selected);
		else
			setBackgroundResource(0);
	}

	@Override
	public void toggle() {
	}

}

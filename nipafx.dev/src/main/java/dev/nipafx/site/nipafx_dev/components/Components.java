package dev.nipafx.site.nipafx_dev.components;

import dev.nipafx.site.nipafx_dev.components.Header.ChannelTags;

import java.util.List;
import java.util.Optional;

public interface Components {

	Layout layout = new Layout(null, List.of());

	ArticleList articleList = new ArticleList();

	static Header header(String title, String description, String head, Optional<ChannelTags> channelTags) {
		return new Header(title, description, head, channelTags);
	}

}

package dev.nipafx.site.nipafx_dev.components;

import dev.nipafx.site.nipafx_dev.components.Header.ChannelTags;
import dev.nipafx.site.nipafx_dev.data.ArticleData;

import java.util.List;
import java.util.Optional;

public interface Components {

	Layout layout = new Layout(null, List.of());

	static Header header(String title, String description, String head, Optional<ChannelTags> channelTags) {
		return new Header(title, description, head, channelTags);
	}

	static ArticleList articleList(List<ArticleData.Page> articles) {
		return new ArticleList(articles);
	}

}

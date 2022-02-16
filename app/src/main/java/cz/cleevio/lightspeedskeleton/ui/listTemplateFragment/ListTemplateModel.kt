package cz.cleevio.lightspeedskeleton.ui.listTemplateFragment

sealed class ListTemplateModel {
	data class Header(val title: String) : ListTemplateModel()
	data class ListItem(val data: ListTemplateItem) : ListTemplateModel()

	companion object {
		const val TYPE_LIST_ITEM = 0
		const val TYPE_HEADER = 1

		fun List<ListTemplateItem>.mapItems(): List<ListTemplateModel> {
			val items: MutableList<ListTemplateModel> = mutableListOf()

			items.add(Header("Title"))
			items.addAll(
				this.map { item ->
					ListItem(
						data = item
					)
				}
			)

			return items
		}
	}
}

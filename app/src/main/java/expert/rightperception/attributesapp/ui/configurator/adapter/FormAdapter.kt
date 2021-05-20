package expert.rightperception.attributesapp.ui.configurator.adapter

import androidx.annotation.NonNull
import expert.rightperception.attributesapp.ui.common.recycler.MultiDiffAdapter
import expert.rightperception.attributesapp.ui.configurator.binder.FormItemViewBinder

class FormAdapter(@NonNull listener: Listener) : MultiDiffAdapter() {

    interface Listener :
//        AttributeItemViewBinder.Listener,
//        NestedAttributeItemViewBinder.Listener,
        FormItemViewBinder.Listener

    init {
//        addBinder(TYPE_TITLE_ITEM, TitleItemViewBinder())
//        addBinder(TYPE_ATTRIBUTE_ITEM, AttributeItemViewBinder(listener))
//        addBinder(TYPE_NESTED_ATTRIBUTE_ITEM, NestedAttributeItemViewBinder(listener))
        addBinder(TYPE_FORM_ITEM, FormItemViewBinder(listener))
    }

    companion object {

//        val TYPE_TITLE_ITEM = 10
//        val TYPE_ATTRIBUTE_ITEM = 20
//        val TYPE_NESTED_ATTRIBUTE_ITEM = 30
        val TYPE_FORM_ITEM = 40
    }

}
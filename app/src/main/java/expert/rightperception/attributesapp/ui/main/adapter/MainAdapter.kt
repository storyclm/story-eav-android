package expert.rightperception.attributesapp.ui.main.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import expert.rightperception.attributesapp.domain.model.ContentModel
import expert.rightperception.attributesapp.ui.configurator.ConfiguratorFragment
import expert.rightperception.attributesapp.ui.content.ContentFragment

class MainAdapter(fragmentActivity: FragmentActivity, private val contentModel: ContentModel) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return if (position == 0) {
            ContentFragment.newInstance(contentModel.presentationEntity.id, contentModel.injectionScript)
        } else {
            ConfiguratorFragment()
        }
    }

}
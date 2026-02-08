package com.ace77505.firefly

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class MainActivity : BaseActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: DataAdapter
    private lateinit var filterDialogHelper: FilterDialogHelper

    // Views
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvResultCount: TextView
    private lateinit var btnFilter: Button
    private lateinit var btnClearAll: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupRecyclerView()
        setupViewModel()
        setupToolbar()
        setupClickListeners()
        setupFilterDialogHelper()

        // 深色模式按钮颜色设置
        updateButtonColors()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)
        tvResultCount = findViewById(R.id.tvResultCount)
        btnFilter = findViewById(R.id.btnFilter)
        btnClearAll = findViewById(R.id.btnClearAll)
    }

    private fun setupRecyclerView() {
        adapter = DataAdapter(onItemClick = { data ->
            openUrlInBrowser(data.id)
        })
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        lifecycleScope.launch {
            viewModel.isLoading.collectLatest { isLoading ->
                progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        lifecycleScope.launch {
            viewModel.filteredData.collectLatest { data ->
                adapter.updateData(data)
            }
        }

        lifecycleScope.launch {
            viewModel.resultCount.collectLatest { count ->
                tvResultCount.text = "共找到 $count 条结果"
            }
        }
    }

    private fun setupToolbar() {
        setToolbarTitle(getString(R.string.app_name))
    }

    private fun setupClickListeners() {
        btnFilter.setOnClickListener {
            showFilterDialog()
        }

        btnClearAll.setOnClickListener {
            viewModel.clearFilters()
        }
    }

    private fun setupFilterDialogHelper() {
        filterDialogHelper = FilterDialogHelper(this) { newFilterState ->
            viewModel.updateFilterState(newFilterState)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                showSettingsDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showFilterDialog() {
        filterDialogHelper.showFilterDialog(viewModel.filterState.value)
    }

    private fun showSettingsDialog() {
        val availableThemes = ThemeManager.getAvailableThemes()
        val themeNames = availableThemes.map { it.first }
        val themeModes = availableThemes.map { it.second }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, themeNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val spinner = Spinner(this)
        spinner.adapter = adapter

        val currentTheme = ThemeManager.getCurrentTheme(this)
        val currentIndex = themeModes.indexOf(currentTheme)
        if (currentIndex >= 0) spinner.setSelection(currentIndex)

        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.settings))
            .setView(spinner)
            .setPositiveButton(android.R.string.ok) { dlg, _ ->
                val pos = spinner.selectedItemPosition
                if (pos in themeModes.indices) {
                    val selectedTheme = themeModes[pos]
                    if (selectedTheme != currentTheme) {
                        ThemeManager.applyTheme(this, selectedTheme)
                    }
                }
                dlg.dismiss()
            }
            .setNegativeButton(android.R.string.cancel) { dlg, _ -> dlg.dismiss() }
            .create()

        dialog.show()
    }

    private fun openUrlInBrowser(id: String) {
        try {
            val url = "https://jm18c-ghj.cc/album/$id"
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())

            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                Toast.makeText(this, "未找到可用的浏览器应用", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "打开链接失败", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateButtonColors() {
        val isNight = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES
        if (isNight) {
            val nightColor = getColor(R.color.md_theme_dark_onSurface)
            btnFilter.setTextColor(nightColor)
            btnClearAll.setTextColor(nightColor)
        } else {
            val lightColor = getColor(R.color.md_theme_light_onSurface)
            btnFilter.setTextColor(lightColor)
            btnClearAll.setTextColor(lightColor)
        }
    }

    override fun onThemeChanged() {
        super.onThemeChanged()
        updateButtonColors()
    }

    inner class DataAdapter(
        private var dataList: List<FilterData> = emptyList(),
        private val onItemClick: (FilterData) -> Unit
    ) : RecyclerView.Adapter<DataAdapter.ViewHolder>() {

        inner class ViewHolder(
            itemView: View,
            private val onItemClick: (FilterData) -> Unit,
            private val context: Context
        ) : RecyclerView.ViewHolder(itemView) {

            private val tvId: TextView = itemView.findViewById(R.id.tvId)
            private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
            private val tvFilter1: TextView = itemView.findViewById(R.id.tvFilter1)
            private val tvFilter2: TextView = itemView.findViewById(R.id.tvFilter2)
            private val tvFilter3: TextView = itemView.findViewById(R.id.tvFilter3)
            private val tvFilter4: TextView = itemView.findViewById(R.id.tvFilter4)
            private val tvFilter5: TextView = itemView.findViewById(R.id.tvFilter5)
            private val tvUpdateDate: TextView = itemView.findViewById(R.id.tvUpdateDate)

            private var currentData: FilterData? = null

            init {
                itemView.setOnClickListener {
                    currentData?.let { data -> onItemClick(data) }
                }
            }

            fun bind(data: FilterData) {
                currentData = data
                tvId.text = data.id
                tvTitle.text = data.title
                tvFilter1.text = context.getString(R.string.ai_format, getFilter1DisplayValue(data.filter1))
                tvFilter2.text = context.getString(R.string.censored_format, getFilter2DisplayValue(data.filter2))
                tvFilter3.text = context.getString(R.string.full_color_format, getFilter3DisplayValue(data.filter3))
                tvFilter4.text = context.getString(R.string.chunai_format, getFilter4DisplayValue(data.filter4))
                tvFilter5.text = context.getString(R.string.tag_format, getFilter5DisplayValue(data.filter5))
                tvUpdateDate.text = data.updateDate
            }

            private fun getFilter1DisplayValue(value: Int): String = when (value) {
                1 -> context.getString(R.string.yes)
                0 -> context.getString(R.string.no)
                else -> context.getString(R.string.unknown)
            }

            private fun getFilter2DisplayValue(value: String): String {
                return if (value.contains("+")) {
                    value.split("+").joinToString("+") { part ->
                        when (part.trim()) {
                            "-1" -> context.getString(R.string.unknown)
                            "0" -> context.getString(R.string.uncensored)
                            "1" -> context.getString(R.string.c_black_lines)
                            "2" -> context.getString(R.string.c_thin_blur)
                            "3" -> context.getString(R.string.c_thick_blur)
                            "4" -> context.getString(R.string.white)
                            else -> part
                        }
                    }
                } else {
                    when (value) {
                        "-1" -> context.getString(R.string.unknown)
                        "0" -> context.getString(R.string.uncensored)
                        "1" -> context.getString(R.string.c_black_lines)
                        "2" -> context.getString(R.string.c_thin_blur)
                        "3" -> context.getString(R.string.c_thick_blur)
                        "4" -> context.getString(R.string.white)
                        else -> value
                    }
                }
            }

            private fun getFilter3DisplayValue(value: Int): String = when (value) {
                1 -> context.getString(R.string.yes)
                0 -> context.getString(R.string.no)
                else -> context.getString(R.string.unknown)
            }

            private fun getFilter4DisplayValue(value: Int): String = when (value) {
                1 -> context.getString(R.string.yes)
                0 -> context.getString(R.string.no)
                else -> context.getString(R.string.unknown)
            }

            private fun getFilter5DisplayValue(value: String): String {
                return if (value.contains("+")) {
                    value.split("+").joinToString("+") { part ->
                        when (part.trim()) {
                            "-1" -> context.getString(R.string.unknown)
                            "1" -> context.getString(R.string.tag_1p)
                            "2" -> context.getString(R.string.tag_cealus)
                            "3" -> context.getString(R.string.tag_futa)
                            "4" -> context.getString(R.string.tag_3p_or_more)
                            else -> part
                        }
                    }
                } else {
                    when (value) {
                        "-1" -> context.getString(R.string.unknown)
                        "1" -> context.getString(R.string.tag_1p)
                        "2" -> context.getString(R.string.tag_cealus)
                        "3" -> context.getString(R.string.tag_futa)
                        "4" -> context.getString(R.string.tag_3p_or_more)
                        else -> value
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_data, parent, false)
            return ViewHolder(view, onItemClick, parent.context)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(dataList[position])
        }

        override fun getItemCount(): Int = dataList.size

        fun updateData(newData: List<FilterData>) {
            dataList = newData
            notifyDataSetChanged()
        }
    }

    inner class FilterDialogHelper(
        private val activity: AppCompatActivity,
        private val onApplyFilters: (MainViewModel.FilterState) -> Unit
    ) {
        private var dialog: AlertDialog? = null
        private var currentState = MainViewModel.FilterState()

        private lateinit var etSearch: TextInputEditText
        private val chipGroups = mutableMapOf<String, ChipGroup>()
        private val chipValueMaps = mutableMapOf<String, Map<Chip, Any>>()

        private val isDarkMode: Boolean by lazy {
            activity.resources.configuration.uiMode and
                    android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
                    android.content.res.Configuration.UI_MODE_NIGHT_YES
        }

        private fun getPrimaryColor(): Int {
            return if (isDarkMode) {
                activity.getColor(R.color.md_theme_dark_primary)
            } else {
                activity.getColor(R.color.md_theme_light_primary)
            }
        }

        private fun getOnPrimaryColor(): Int {
            return if (isDarkMode) {
                activity.getColor(R.color.md_theme_dark_onPrimary)
            } else {
                activity.getColor(R.color.md_theme_light_onPrimary)
            }
        }

        private fun getSurfaceColor(): Int {
            return if (isDarkMode) {
                activity.getColor(R.color.md_theme_dark_surface)
            } else {
                activity.getColor(R.color.md_theme_light_surface)
            }
        }

        private fun getOnSurfaceColor(): Int {
            return if (isDarkMode) {
                activity.getColor(R.color.md_theme_dark_onSurface)
            } else {
                activity.getColor(R.color.md_theme_light_onSurface)
            }
        }

        private fun getPrimaryContainerColor(): Int {
            return if (isDarkMode) {
                activity.getColor(R.color.md_theme_dark_primaryContainer)
            } else {
                activity.getColor(R.color.md_theme_light_primaryContainer)
            }
        }

        private fun getOnPrimaryContainerColor(): Int {
            return if (isDarkMode) {
                activity.getColor(R.color.md_theme_dark_onPrimaryContainer)
            } else {
                activity.getColor(R.color.md_theme_light_onPrimaryContainer)
            }
        }

        private fun getOutlineColor(): Int {
            return if (isDarkMode) {
                0xFFAAAAAA.toInt()
            } else {
                getPrimaryColor()
            }
        }

        private fun getSelectedOutlineColor(): Int {
            return if (isDarkMode) {
                0xFF4FC3F7.toInt()
            } else {
                getPrimaryColor()
            }
        }

        fun showFilterDialog(initialState: MainViewModel.FilterState) {
            currentState.copyFrom(initialState)

            if (dialog == null) {
                createDialog()
            } else {
                updateDialogState()
            }

            dialog?.show()
        }

        private fun createDialog() {
            val scrollView = ScrollView(activity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val container = LinearLayout(activity).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(24.dp, 20.dp, 24.dp, 20.dp)
                background = createSurfaceBackground()
            }

            container.addView(createSearchField())
            container.addView(createSpacer(24.dp))

            container.addView(createSectionTitle(activity.getString(R.string.recommended)))
            container.addView(createSpacer(8.dp))
            val recommendGroup = createChipGroup(
                mapOf(
                    activity.getString(R.string.yes) to 1,
                    activity.getString(R.string.no) to 0
                ),
                currentState.selectedRecommend,
                "recommend"
            )
            container.addView(recommendGroup)
            chipGroups["recommend"] = recommendGroup

            container.addView(createSpacer(20.dp))

            container.addView(createSectionTitle(activity.getString(R.string.ai)))
            container.addView(createSpacer(8.dp))
            val filter1Group = createChipGroup(
                mapOf(
                    activity.getString(R.string.yes) to 1,
                    activity.getString(R.string.no) to 0
                ),
                currentState.selectedFilter1,
                "filter1"
            )
            container.addView(filter1Group)
            chipGroups["filter1"] = filter1Group

            container.addView(createSpacer(20.dp))

            container.addView(createSectionTitle(activity.getString(R.string.censored_type)))
            container.addView(createSpacer(8.dp))
            val filter2Group = createChipGroup(
                mapOf(
                    activity.getString(R.string.unknown) to "-1",
                    activity.getString(R.string.uncensored) to "0",
                    activity.getString(R.string.c_black_lines) to "1",
                    activity.getString(R.string.c_thin_blur) to "2",
                    activity.getString(R.string.c_thick_blur) to "3",
                    activity.getString(R.string.white) to "4"
                ),
                currentState.selectedFilter2,
                "filter2"
            )
            container.addView(filter2Group)
            chipGroups["filter2"] = filter2Group

            container.addView(createSpacer(20.dp))

            container.addView(createSectionTitle(activity.getString(R.string.full_color)))
            container.addView(createSpacer(8.dp))
            val filter3Group = createChipGroup(
                mapOf(
                    activity.getString(R.string.unknown) to -1,
                    activity.getString(R.string.yes) to 1,
                    activity.getString(R.string.no) to 0
                ),
                currentState.selectedFilter3,
                "filter3"
            )
            container.addView(filter3Group)
            chipGroups["filter3"] = filter3Group

            container.addView(createSpacer(20.dp))

            container.addView(createSectionTitle(activity.getString(R.string.chunai)))
            container.addView(createSpacer(8.dp))
            val filter4Group = createChipGroup(
                mapOf(
                    activity.getString(R.string.unknown) to -1,
                    activity.getString(R.string.yes) to 1,
                    activity.getString(R.string.no) to 0
                ),
                currentState.selectedFilter4,
                "filter4"
            )
            container.addView(filter4Group)
            chipGroups["filter4"] = filter4Group

            container.addView(createSpacer(20.dp))

            container.addView(createSectionTitle(activity.getString(R.string.tag)))
            container.addView(createSpacer(8.dp))
            val filter5Group = createChipGroup(
                mapOf(
                    activity.getString(R.string.unknown) to "-1",
                    activity.getString(R.string.tag_1p) to "1",
                    activity.getString(R.string.tag_cealus) to "2",
                    activity.getString(R.string.tag_futa) to "3",
                    activity.getString(R.string.tag_3p_or_more) to "4"
                ),
                currentState.selectedFilter5,
                "filter5"
            )
            container.addView(filter5Group)
            chipGroups["filter5"] = filter5Group

            container.addView(createSpacer(28.dp))
            container.addView(createButtons())

            scrollView.addView(container)

            dialog = AlertDialog.Builder(activity)
                .setTitle("筛选条件")
                .setView(scrollView)
                .create()

            dialog?.window?.setBackgroundDrawable(createDialogBackground())
        }

        private fun createSearchField(): TextInputLayout {
            val textInputLayout = TextInputLayout(activity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

                boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
                hint = activity.getString(R.string.search_id_or_name)
                isHintEnabled = true

                shapeAppearanceModel = ShapeAppearanceModel.Builder()
                    .setAllCorners(CornerFamily.ROUNDED, 12.dp.toFloat())
                    .build()
            }

            etSearch = TextInputEditText(activity).apply {
                setText(currentState.searchText)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(16.dp, 14.dp, 16.dp, 14.dp)
            }

            textInputLayout.addView(etSearch)
            return textInputLayout
        }

        private fun createSectionTitle(text: String): TextView {
            return TextView(activity).apply {
                this.text = text
                textSize = 14f
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(getOnSurfaceColor())
                alpha = 0.6f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
        }

        @Suppress("UNCHECKED_CAST")
        private fun <T> createChipGroup(
            chipLabelValueMap: Map<String, T>,
            selectedValues: Set<T>?,
            groupKey: String
        ): ChipGroup {
            return ChipGroup(activity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                isSingleSelection = false
                chipSpacingHorizontal = 8.dp
                chipSpacingVertical = 8.dp

                val chipValueMap = mutableMapOf<Chip, Any>()

                chipLabelValueMap.forEach { (label, value) ->
                    val chip = createChip(label, selectedValues?.contains(value) ?: false)
                    addView(chip)
                    chipValueMap[chip] = value as Any
                }

                chipValueMaps[groupKey] = chipValueMap
            }
        }

        private fun createChip(label: String, isInitiallyChecked: Boolean): Chip {
            return Chip(activity).apply {
                text = label
                isCheckable = true
                isChecked = isInitiallyChecked

                shapeAppearanceModel = ShapeAppearanceModel.Builder()
                    .setAllCorners(CornerFamily.ROUNDED, 8.dp.toFloat())
                    .build()

                updateChipStyle(this, isInitiallyChecked)

                setOnCheckedChangeListener { _, isChecked -> updateChipStyle(this, isChecked) }

                layoutParams = ChipGroup.LayoutParams(
                    ChipGroup.LayoutParams.WRAP_CONTENT,
                    ChipGroup.LayoutParams.WRAP_CONTENT
                )

                setPadding(12.dp, 8.dp, 12.dp, 8.dp)
            }
        }

        private fun updateChipStyle(chip: Chip, isChecked: Boolean) {
            if (isDarkMode) {
                if (isChecked) {
                    chip.chipBackgroundColor = ColorStateList.valueOf(getPrimaryColor().withAlpha(180))
                    chip.setTextColor(getOnPrimaryColor())
                    chip.chipStrokeColor = ColorStateList.valueOf(getSelectedOutlineColor())
                    chip.chipStrokeWidth = 2.5f.dp
                    chip.elevation = 2.dp.toFloat()
                } else {
                    chip.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(activity, android.R.color.transparent))
                    chip.setTextColor(getOnSurfaceColor())
                    chip.chipStrokeColor = ColorStateList.valueOf(getOutlineColor())
                    chip.chipStrokeWidth = 1.5f.dp
                    chip.elevation = 0f
                }
            } else {
                if (isChecked) {
                    chip.chipBackgroundColor = ColorStateList.valueOf(getPrimaryContainerColor())
                    chip.setTextColor(getOnPrimaryContainerColor())
                    chip.chipStrokeColor = ColorStateList.valueOf(getSelectedOutlineColor())
                    chip.chipStrokeWidth = 2.dp.toFloat()
                    chip.elevation = 2.dp.toFloat()
                } else {
                    chip.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(activity, android.R.color.transparent))
                    chip.setTextColor(getOnSurfaceColor())
                    chip.chipStrokeColor = ColorStateList.valueOf(getOutlineColor())
                    chip.chipStrokeWidth = 1.dp.toFloat()
                    chip.elevation = 0f
                }
            }
        }

        private fun createButtons(): LinearLayout {
            return LinearLayout(activity).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

                val clearButton = MaterialButton(activity).apply {
                    text = activity.getString(R.string.clear)
                    setTextColor(getOnSurfaceColor())
                    strokeColor = ColorStateList.valueOf(getOutlineColor())
                    strokeWidth = 2.dp
                    setBackgroundColor(ContextCompat.getColor(activity, android.R.color.transparent))

                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    ).apply { marginEnd = 8.dp }

                    cornerRadius = 20.dp
                    setPadding(16.dp, 12.dp, 16.dp, 12.dp)

                    setOnClickListener { clearDialogSelections() }
                }

                val applyButton = MaterialButton(activity).apply {
                    text = activity.getString(R.string.apply)
                    setTextColor(getOnSurfaceColor())
                    strokeColor = ColorStateList.valueOf(getOutlineColor())
                    strokeWidth = 2.dp
                    setBackgroundColor(ContextCompat.getColor(activity, android.R.color.transparent))

                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    ).apply { marginStart = 8.dp }

                    cornerRadius = 20.dp
                    setPadding(16.dp, 12.dp, 16.dp, 12.dp)

                    setOnClickListener {
                        applyFilters()
                        dialog?.dismiss()
                    }
                }

                addView(clearButton)
                addView(applyButton)
            }
        }

        private fun createSpacer(height: Int): android.view.View {
            return android.view.View(activity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    height
                )
            }
        }

        private fun createSurfaceBackground(): MaterialShapeDrawable {
            return MaterialShapeDrawable(
                ShapeAppearanceModel.Builder()
                    .setAllCorners(CornerFamily.ROUNDED, 16.dp.toFloat())
                    .build()
            ).apply {
                fillColor = ColorStateList.valueOf(getSurfaceColor())
            }
        }

        private fun createDialogBackground(): MaterialShapeDrawable {
            return MaterialShapeDrawable(
                ShapeAppearanceModel.Builder()
                    .setAllCorners(CornerFamily.ROUNDED, 28.dp.toFloat())
                    .build()
            ).apply {
                fillColor = ColorStateList.valueOf(getSurfaceColor())
            }
        }

        private fun updateDialogState() {
            etSearch.setText(currentState.searchText)

            updateChipGroupState("recommend", currentState.selectedRecommend)
            updateChipGroupState("filter1", currentState.selectedFilter1)
            updateChipGroupState("filter2", currentState.selectedFilter2)
            updateChipGroupState("filter3", currentState.selectedFilter3)
            updateChipGroupState("filter4", currentState.selectedFilter4)
            updateChipGroupState("filter5", currentState.selectedFilter5)
        }

        @Suppress("UNCHECKED_CAST")
        private fun <T> updateChipGroupState(groupKey: String, selectedValues: Set<T>?) {
            val chipMap = chipValueMaps[groupKey] ?: return
            chipMap.forEach { (chip, rawValue) ->
                val value = rawValue as? T
                val shouldBeChecked = selectedValues?.contains(value) ?: false
                chip.isChecked = shouldBeChecked
                updateChipStyle(chip, shouldBeChecked)
            }
        }

        private fun clearDialogSelections() {
            chipGroups.values.forEach { it.clearCheck() }
            chipValueMaps.values.forEach { chipMap ->
                chipMap.keys.forEach { chip ->
                    chip.isChecked = false
                    updateChipStyle(chip, false)
                }
            }
            etSearch.setText("")
        }

        private fun applyFilters() {
            currentState.searchText = etSearch.text.toString()

            currentState.selectedRecommend = getSelectedValuesAsInt("recommend")
            currentState.selectedFilter1 = getSelectedValuesAsInt("filter1")
            currentState.selectedFilter2 = getSelectedValuesAsString("filter2")
            currentState.selectedFilter3 = getSelectedValuesAsInt("filter3")
            currentState.selectedFilter4 = getSelectedValuesAsInt("filter4")
            currentState.selectedFilter5 = getSelectedValuesAsString("filter5")

            onApplyFilters(currentState)
        }

        @Suppress("UNCHECKED_CAST")
        private fun getSelectedValuesAsInt(groupKey: String): Set<Int>? {
            val chipMap = chipValueMaps[groupKey] ?: return null
            val selectedValues = mutableSetOf<Int>()

            chipMap.forEach { (chip, rawValue) ->
                if (chip.isChecked) {
                    when (rawValue) {
                        is Int -> selectedValues.add(rawValue)
                        is String -> rawValue.toIntOrNull()?.let { selectedValues.add(it) }
                    }
                }
            }

            return if (selectedValues.isEmpty()) null else selectedValues
        }

        @Suppress("UNCHECKED_CAST")
        private fun getSelectedValuesAsString(groupKey: String): Set<String>? {
            val chipMap = chipValueMaps[groupKey] ?: return null
            val selectedValues = mutableSetOf<String>()

            chipMap.forEach { (chip, rawValue) ->
                if (chip.isChecked) {
                    when (rawValue) {
                        is String -> selectedValues.add(rawValue)
                        is Int -> selectedValues.add(rawValue.toString())
                        else -> rawValue.toString().let { selectedValues.add(it) }
                    }
                }
            }

            return if (selectedValues.isEmpty()) null else selectedValues
        }

        private val Int.dp: Int
            get() = (this * activity.resources.displayMetrics.density).toInt()

        private val Float.dp: Float
            get() = this * activity.resources.displayMetrics.density

        private fun Int.withAlpha(alpha: Int): Int {
            return (alpha shl 24) or (this and 0x00FFFFFF)
        }
    }
}

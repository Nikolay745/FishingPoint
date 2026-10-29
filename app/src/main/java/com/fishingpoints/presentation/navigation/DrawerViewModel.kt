package com.fishingpoints.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fishingpoints.data.local.database.dao.BaitDao
import com.fishingpoints.data.local.database.dao.DailyAllowanceDao
import com.fishingpoints.data.local.database.dao.FeedDao
import com.fishingpoints.data.local.database.dao.FishDao
import com.fishingpoints.data.local.database.dao.FishingCatchDao
import com.fishingpoints.data.local.database.dao.LocationDao
import com.fishingpoints.data.local.database.dao.RegionDao
import com.fishingpoints.data.local.database.dao.TackleDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DrawerViewModel @Inject constructor(
    diaryDao: FishingCatchDao,
    fishDao: FishDao,
    fishLimitDao: DailyAllowanceDao,
    regionDao: RegionDao,
    locationDao: LocationDao,
    baitDao: BaitDao,
    feedDao: FeedDao,
    tackleDao: TackleDao
) : ViewModel() {

    val diaryCount = diaryDao.getCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)

    val fishCount = fishDao.observeFishCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)

    val limitCount = fishLimitDao.getCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)

    val regionCount = regionDao.getCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)

    val locationCount = locationDao.getCount()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)

    val baitsCount = baitDao.getCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)

    val feedCount = feedDao.getCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)

}
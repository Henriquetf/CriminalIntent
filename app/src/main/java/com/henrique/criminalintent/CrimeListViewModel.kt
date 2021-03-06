package com.henrique.criminalintent

import androidx.lifecycle.ViewModel

class CrimeListViewModel : ViewModel() {
    private val crimeRepository = CrimeRepository.get()

    val crimeListLiveData = crimeRepository.getAllCrimes()

    fun addCrime(crime: Crime) {
        crimeRepository.insertCrime(crime)
    }
}

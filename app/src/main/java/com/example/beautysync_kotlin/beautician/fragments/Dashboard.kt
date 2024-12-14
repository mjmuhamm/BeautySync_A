package com.example.beautysync_kotlin.beautician.fragments

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.beautysync_kotlin.R
import com.example.beautysync_kotlin.beautician.models.ServiceItem
import com.example.beautysync_kotlin.both.models.Orders
import com.example.beautysync_kotlin.databinding.FragmentDashboardBinding
import com.example.beautysync_kotlin.databinding.FragmentMe2Binding
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Date

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Dashboard.newInstance] factory method to
 * create an instance of this fragment.
 */
private const val TAG = "Dashboard"
class Dashboard : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    private val httpClient = OkHttpClient()
    private val mHandler: Handler = Handler(Looper.getMainLooper())

    private var _binding : FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private var serviceItems: MutableList<ServiceItem> = ArrayList()
    private var serviceItemArray = arrayListOf<String>()

    private lateinit var serviceArrayAdapter: ArrayAdapter<String>

    //Compare Dates
    @SuppressLint("SimpleDateFormat")
    private var sdfMonth = SimpleDateFormat("MM")

    @SuppressLint("SimpleDateFormat")
    private var sdfDay = SimpleDateFormat("dd")

    @SuppressLint("SimpleDateFormat")
    private var sdfYear = SimpleDateFormat("yyyy")

    @SuppressLint("SimpleDateFormat")
    private var sdfHour = SimpleDateFormat("hh")

    @SuppressLint("SimpleDateFormat")
    private var sdfMin = SimpleDateFormat("mm")

    @SuppressLint("SimpleDateFormat")
    private var sdfAmOrPm = SimpleDateFormat("a")

    @SuppressLint("SimpleDateFormat")
    private val sdf = SimpleDateFormat("MM-dd-yyyy")

    @SuppressLint("SimpleDateFormat")
    private var sdfYearMonth = SimpleDateFormat("yyyy, MM")

    private var toggle = "Weekly"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)

        val typeOfService = resources.getStringArray(R.array.type_of_service)
        val typeOfServiceAll = resources.getStringArray(R.array.type_of_service_all)

        var typeOfServiceAdapter =
            ArrayAdapter(requireContext(), R.layout.dropdown_item, typeOfService)
        binding.typeOfServiceText.setAdapter(typeOfServiceAdapter)

        binding.typeOfServiceText.setOnItemClickListener { _, _, _, _ ->
            binding.menuItemText.setText("")
            val x = binding.typeOfServiceText.text.toString()
            var service = ""
            when (x) {
                "Hair Care Items" -> { service = "hairCareItems" }
                "Skin Care Items" -> { service = "skinCareItems" }
                "Nail Care Items" -> { service = "nailCareItems" }
            }
            if (binding.typeOfServiceText.text.isNotEmpty()) {
                if (binding.typeOfServiceText.text.toString() != "All Items") {
                    if (toggle == "Total") {
                        //loadTotalData
                        loadTotalData(service)
                    } else {
                        loadServiceItems(service)
                    }
                } else {
                    loadAllTotalData()
                }
             }
        }

        binding.menuItemText.setOnItemClickListener { _, _, _, _ ->
            val x = binding.typeOfServiceText.text.toString()
            var service = ""
            when (x) {
                "Hair Care Items" -> { service = "hairCareItems" }
                "Skin Care Items" -> { service = "skinCareItems" }
                "Nail Care Items" -> { service = "nailCareItems" }
            }

            if (binding.menuItemText.text.isNotEmpty()) {
                val index =
                    serviceItems.indexOfFirst { it.itemTitle == binding.menuItemText.text.toString() }
                if (toggle == "Weekly") {
                    loadWeeklyData(service, serviceItems[index].documentId)
                } else if (toggle == "Monthly") {
                    //loadMonthlyData
                    loadMonthlyData(service, serviceItems[index].documentId)
                }
            }
        }

        binding.weekly.setOnClickListener {
            toggle = "Weekly"

            binding.typeOfServiceText.setText("")
            binding.menuItemText.setText("")

            binding.weeklyBarChart.isVisible = true
            binding.monthlyBarChart.isVisible = false
            binding.totalPieChart.isVisible = false

            binding.menuItemLayout.visibility = View.VISIBLE

            typeOfServiceAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, typeOfService)
            binding.typeOfServiceText.setAdapter(typeOfServiceAdapter)

            binding.weekly.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary))
            binding.weekly.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.monthly.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.monthly.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.total.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.total.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
        }

        binding.monthly.setOnClickListener {
            toggle = "Monthly"

            binding.typeOfServiceText.setText("")
            binding.menuItemText.setText("")

            binding.weeklyBarChart.isVisible = false
            binding.monthlyBarChart.isVisible = true
            binding.totalPieChart.isVisible = false

            binding.menuItemLayout.visibility = View.VISIBLE

            typeOfServiceAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, typeOfService)
            binding.typeOfServiceText.setAdapter(typeOfServiceAdapter)

            binding.weekly.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.weekly.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.monthly.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary))
            binding.monthly.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.total.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.total.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
        }

        binding.total.setOnClickListener {
            toggle = "Total"

            binding.typeOfServiceText.setText("")
            binding.menuItemText.setText("")

            binding.weeklyBarChart.isVisible = false
            binding.monthlyBarChart.isVisible = false
            binding.totalPieChart.isVisible = true

            binding.menuItemLayout.visibility = View.GONE

            typeOfServiceAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, typeOfServiceAll)
            binding.typeOfServiceText.setAdapter(typeOfServiceAdapter)

            binding.weekly.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.weekly.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.monthly.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.monthly.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.total.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary))
            binding.total.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))


        }
        loadOrders()
        // Inflate the layout for this fragment
        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Dashboard.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Dashboard().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun loadServiceItems(service: String) {
        serviceItems.clear()
        serviceItemArray.clear()

        db.collection("Beautician").document(auth.currentUser!!.uid).collection(service).get().addOnSuccessListener { documents ->
            if (documents != null) {
                for (doc in documents.documents) {
                    val data = doc.data

                    val itemTitle = data?.get("itemTitle") as String
                    val x = ServiceItem(doc.id, itemTitle)

                    if (serviceItems.isEmpty()) {
                        serviceItems.add(x)
                        serviceItemArray.add(itemTitle)

                        serviceArrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, serviceItemArray)

                        binding.menuItemText.setAdapter(serviceArrayAdapter)
                    } else {
                        val index = serviceItems.indexOfFirst { it.documentId == doc.id }
                        if (index == -1) {
                            serviceItems.add(x)
                            serviceItemArray.add(itemTitle)

                            serviceArrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, serviceItemArray)

                            binding.menuItemText.setAdapter(serviceArrayAdapter)
                        }
                    }
                }
            }
        }
    }

    private fun loadWeeklyData(service: String, menuItemId: String) {
        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(0F, 0F))
        entries.add(BarEntry(1F, 0F))
        entries.add(BarEntry(2F, 0F))
        entries.add(BarEntry(3F, 0F))


        val labels = ArrayList<String>()
        labels.add("Week 1")
        labels.add("Week 2")
        labels.add("Week 3")
        labels.add("Week 4")

        val colors = ArrayList<Int>()
        for (i in 0 until ColorTemplate.PASTEL_COLORS.size) {
            colors.add(ColorTemplate.PASTEL_COLORS[i])
        }

        val yearMonth = sdfYearMonth.format(Date())

        var weekOfMonth = Calendar.getInstance()[Calendar.WEEK_OF_MONTH]
        if (weekOfMonth > 5) { weekOfMonth = 4 }


        for (i in 1 until 5) {
            val week = "Week $i"
            Log.d(TAG, "loadWeeklyData: Week $i")
            db.collection("Beautician").document(FirebaseAuth.getInstance().currentUser!!.uid)
                .collection("Dashboard").document(service)
                .collection(menuItemId).document("Month").collection(yearMonth).document("Week")
                .collection(week).get()
                .addOnSuccessListener { documents ->
                    if (documents != null) {
                        for (doc in documents.documents) {
                            val data = doc.data
                            val total = data?.get("totalPay") as Number
                            val num = i-1
                            entries[num] = BarEntry(num.toFloat(), total.toFloat())
                        }

                        val barDataSet = BarDataSet(entries, service)
                        barDataSet.colors = colors
                        val barData = BarData(barDataSet)

                        binding.weeklyBarChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                        binding.weeklyBarChart.xAxis.position = XAxis.XAxisPosition.BOTTOM

                        binding.weeklyBarChart.setDrawGridBackground(false)
                        binding.weeklyBarChart.xAxis.setDrawAxisLine(false)
                        binding.weeklyBarChart.xAxis.setDrawGridLines(false)
                        binding.weeklyBarChart.xAxis.labelCount = 4
                        binding.weeklyBarChart.axisLeft.isEnabled = false
                        binding.weeklyBarChart.axisRight.isEnabled = false
                        binding.weeklyBarChart.description.isEnabled = false

                        binding.weeklyBarChart.setScaleEnabled(false)
                        binding.weeklyBarChart.animateXY(1500, 1500)
                        binding.weeklyBarChart.data = barData
                        binding.weeklyBarChart.invalidate()

                    }
                }
        }
    }

    private fun loadMonthlyData(service: String, menuItemId: String) {
        val entries = ArrayList<BarEntry>()
        val year = sdfYear.format(Date())
        var month : String

        val labels = ArrayList<String>()
        if (Calendar.getInstance()[Calendar.MONTH] < 6) {
            labels.add("January")
            labels.add("February")
            labels.add("March")
            labels.add("April")
            labels.add("May")
            labels.add("June")
            month = "1"
        } else {
            labels.add("July")
            labels.add("August")
            labels.add("September")
            labels.add("October")
            labels.add("November")
            labels.add("December")
            month = "7"
        }

        val colors = ArrayList<Int>()
        for (i in 0 until ColorTemplate.PASTEL_COLORS.size) {
            colors.add(ColorTemplate.PASTEL_COLORS[i])
        }




        for (i in 0 until 6) {
            if (month.toInt() < 10) { month = "0$month" }
            val yearMonth = "$year, $month"
            month = "${month.toInt() + 1}"
            db.collection("Beautician").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("Dashboard").document(service)
                .collection(menuItemId).document("Month").collection(yearMonth).document("Total")
                .get().addOnSuccessListener { document ->
                    if (document.exists()) {
                        val data = document.data
                        val total = data?.get("totalPay") as Number
                        entries.add(BarEntry(i.toFloat(), total.toFloat()))
                    } else {
                        entries.add(BarEntry(i.toFloat(), 0F)) }

                    val barDataSet = BarDataSet(entries, service)
                    barDataSet.colors = colors
                    val barData = BarData(barDataSet)

                    binding.monthlyBarChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                    binding.monthlyBarChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
                    binding.monthlyBarChart.setDrawGridBackground(false)
                    binding.monthlyBarChart.xAxis.setDrawAxisLine(false)
                    binding.monthlyBarChart.xAxis.setDrawGridLines(false)
                    binding.monthlyBarChart.axisLeft.isEnabled = false
                    binding.monthlyBarChart.axisRight.isEnabled = false
                    binding.monthlyBarChart.description.isEnabled = false


                    binding.monthlyBarChart.animateXY(1500, 1500)
                    binding.monthlyBarChart.data = barData
                    binding.monthlyBarChart.invalidate()

                }
        }
    }

    private fun loadTotalData(service: String) {

        val entries = ArrayList<PieEntry>()

        val colors = ArrayList<Int>()
        for (i in 0 until ColorTemplate.PASTEL_COLORS.size) {
            colors.add(ColorTemplate.PASTEL_COLORS[i])
        }

        var c = ""
        when (service) {
            "hairCareItems" -> { c = "Hair Care Items" }
            "skinCareItems" -> { c = "Skin Care Items" }
            "nailCareItems" -> { c = "Nail Care Items" }
        }

        binding.totalPieChart.isDrawHoleEnabled = true
        binding.totalPieChart.setUsePercentValues(false)
        binding.totalPieChart.setEntryLabelTextSize(0f)
        binding.totalPieChart.setEntryLabelColor(ContextCompat.getColor(requireContext(), R.color.white))
        binding.totalPieChart.centerText = c
        binding.totalPieChart.setCenterTextColor(ContextCompat.getColor(requireContext() ,R.color.main))
        binding.totalPieChart.setCenterTextSize(17f)
        binding.totalPieChart.description.isEnabled = false

        val l = binding.totalPieChart.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        l.orientation = Legend.LegendOrientation.VERTICAL
        l.setDrawInside(false)
        l.isEnabled = true

        db.collection("Beautician").document(FirebaseAuth.getInstance().currentUser!!.uid).collection(service).get().addOnSuccessListener { documents ->
            if (documents != null) {
                for (doc in documents.documents) {
                    val data = doc.data


                        val itemTitle = data?.get("itemTitle") as String
                        val documentId = doc.id

                        db.collection("Beautician").document(FirebaseAuth.getInstance().currentUser!!.uid)
                            .collection("Dashboard").document(service).collection(documentId)
                            .document("Total").get().addOnSuccessListener { document ->
                                if (document.exists()) {
                                    val data1 = document.data

                                    val total = data1?.get("totalPay") as Number
                                    entries.add(PieEntry(total.toFloat(), itemTitle))
                                } else {
                                    entries.add(PieEntry(0F, itemTitle))
                                }

                                val dataSet = PieDataSet(entries, "")
                                dataSet.colors = colors
                                dataSet.valueFormatter = PercentFormatter(binding.totalPieChart)
                                dataSet.valueTextSize = 12f
                                dataSet.valueTextColor = ContextCompat.getColor(
                                    requireContext(),
                                    R.color.white
                                )
                                val data2 = PieData(dataSet)
                                binding.totalPieChart.data = data2
                                binding.totalPieChart.invalidate()

                                binding.totalPieChart.animateXY(1250, 1250)
                            }
                    }
                }
        }

    }

    private fun loadAllTotalData() {

        val entries = ArrayList<PieEntry>()

        val colors = ArrayList<Int>()
        for (i in 0 until ColorTemplate.PASTEL_COLORS.size) {
            colors.add(ColorTemplate.PASTEL_COLORS[i])
        }

        binding.totalPieChart.isDrawHoleEnabled = true
        binding.totalPieChart.setUsePercentValues(false)
        binding.totalPieChart.setEntryLabelTextSize(12f)
        binding.totalPieChart.setEntryLabelColor(ContextCompat.getColor(requireContext(), R.color.white))
        binding.totalPieChart.centerText = "All Items"
        binding.totalPieChart.setCenterTextColor(ContextCompat.getColor(requireContext() ,R.color.main))
        binding.totalPieChart.setCenterTextSize(17f)
        binding.totalPieChart.description.isEnabled = false

        val l = binding.totalPieChart.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        l.orientation = Legend.LegendOrientation.VERTICAL
        l.setDrawInside(false)
        l.isEnabled = true

        val typeOfService = arrayListOf("Hair Care Items", "Skin Care Items", "Nail Care Items")



        for (i in 0 until typeOfService.size) {
            var b = typeOfService[i]
            var c = ""
            if (b == "Hair Care Items") { c = "hairCareItems" } else if (b == "Skin Care Items") { c = "skinCareItems" } else if (b == "Nail Care Items") { c = "nailCareItems" }
            Log.d(TAG, "loadAllTotalData: $b")
            db.collection("Beautician").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("Dashboard").document(c).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val data = document.data
                        val total = data?.get("totalPay") as Number
                        entries.add(PieEntry(total.toFloat(), typeOfService[i]))
                    } else {
                        entries.add(PieEntry(0F, typeOfService[i]))
                    }

                    val dataSet = PieDataSet(entries, "")
                    dataSet.colors = colors
                    dataSet.setDrawValues(true)
                    dataSet.valueFormatter = PercentFormatter(binding.totalPieChart)
                    dataSet.valueTextSize = 12f
                    dataSet.valueTextColor = ContextCompat.getColor(requireContext(), R.color.white)
                    val data = PieData(dataSet)
                    binding.totalPieChart.data = data
                    binding.totalPieChart.invalidate()

                    binding.totalPieChart.animateXY(1250,1250)


                }
        }
    }

    private fun loadOrders() {

        db.collection("Beautician").document(auth.currentUser!!.uid).collection("Orders").addSnapshotListener { documents, _ ->
            if (documents != null) {
                for (doc in documents.documents) {
                    val data = doc.data

                    val beauticianImageId = data?.get("beauticianImageId") as String
                    val eventDay = data["eventDay"] as String
                    val eventTime = data["eventTime"] as String
                    val userImageId = data["userImageId"] as String
                    val status = data["status"] as String


                    compareDates(status,"$eventDay $eventTime", doc.id, beauticianImageId, userImageId)


                }
            }
        }
    }

    private fun compareDates(status: String, date: String, documentId: String, beauticianImageId: String, userImageId: String) : Boolean {
        val month1 = sdfMonth.format(Date())
        val day1 = sdfDay.format(Date())
        val year1 = sdfYear.format(Date())
        val date1 = sdf.format(Date())
        val hour1 = sdfHour.format(Date())
        val min1 = sdfMin.format(Date())
        val amOrPm1 = sdfAmOrPm.format(Date())


        val month2 = date.substring(0, 2)
        val day2 = date.substring(3,5)
        val year2 = date.substring(6,10)
        val date2 = date
        val hour2 = date.substring(11,13)
        val min2 = date.substring(14,16)
        val amOrPm2 = date.substring(17,19)

        if (status == "scheduled") {
            if (year1.toInt() > year2.toInt()) {
                //pass
                Log.d(TAG, "compareDates: pass1")
                transferStatus(documentId, beauticianImageId, userImageId)
                return true
            } else if (year1.toInt() == year2.toInt()) {
                if (month1.toInt() > month2.toInt()) {
                    //pass
                    Log.d(TAG, "compareDates: pass2")
                    transferStatus(documentId, beauticianImageId, userImageId)
                    return true
                } else {
                    if (month1.toInt() == month2.toInt()) {
                        if (day1.toInt() > day2.toInt()) {
                            //pass
                            Log.d(TAG, "compareDates: pass3")
                            transferStatus(documentId, beauticianImageId, userImageId)
                            return true
                        } else {
                            if (day1.toInt() == day2.toInt()) {
                                if (amOrPm1 == "PM" && amOrPm2 == "AM") {
                                    when {
                                        hour1.toInt() > 1 -> {
                                            //pass
                                            Log.d(TAG, "compareDates: pass4")
                                            transferStatus(
                                                documentId,
                                                beauticianImageId,
                                                userImageId
                                            )
                                            return true
                                        }

                                        min1.toInt() >= min2.toInt() -> {
                                            //pass
                                            Log.d(TAG, "compareDates: pass5")
                                            transferStatus(
                                                documentId,
                                                beauticianImageId,
                                                userImageId
                                            )
                                            return true
                                        }
                                    }

                                } else if ((amOrPm1 == "AM" && amOrPm2 == "AM") || amOrPm1 == "PM" && amOrPm2 == "PM") {
                                    if (hour1.toInt() - hour2.toInt() > 1) {
                                        //pass
                                        Log.d(TAG, "compareDates: pass6")
                                        transferStatus(documentId, beauticianImageId, userImageId)
                                        return true
                                    } else if (hour1.toInt() > hour2.toInt() && (min1.toInt() >= min2.toInt())) {
                                        //pass
                                        Log.d(TAG, "compareDates: pass7")
                                        transferStatus(documentId, beauticianImageId, userImageId)
                                        return true
                                    }

                                }
                            }
                        }
                    }
                }
            }
        }
        return false
    }

    private fun transferStatus(documentId: String, beauticianImageId: String, userImageId: String) {

        db.collection("Orders").document(documentId).get().addOnSuccessListener { document ->
            if (document != null) {
                val data1 = document.data

                val status = data1?.get("status") as String
                val itemPrice = data1["itemPrice"] as String
                val eventDay = data1["eventDay"] as String
                val eventTime = data1["eventTime"] as String

                if (status != "cancelled" && status != "complete") {
                    transferFunds(itemPrice, documentId, "$eventDay $eventTime", beauticianImageId, userImageId)
                    val data : Map<String, Any> = hashMapOf("status" to "complete")
                    db.collection("User").document(userImageId).collection("Orders").document(documentId).update(data)
                    db.collection("Beautician").document(beauticianImageId).collection("Orders").document(documentId).update(data)
                    db.collection("Orders").document(documentId).update(data)
                }
            }
        }
    }

    private fun transferFunds(itemPrice: String, orderId: String, eventDate: String, beauticianImageId: String, userImageId: String) {

        val date = Calendar.getInstance().timeInMillis / 1000
        val x = itemPrice.toDouble() * 0.95 * 100
        val y = "%.0f".format(x)
        db.collection("Beautician").document(beauticianImageId).collection("BankingInfo").get().addOnSuccessListener { documents ->
            if (documents != null) {
                for (doc in documents.documents) {
                    val data = doc.data

                    val stripeAccountId = data?.get("stripeAccountId") as String


                    val body = FormBody.Builder()
                        .add("amount", y)
                        .add("stripeAccountId", stripeAccountId)
                        .build()

                    val request = Request.Builder()
                        .url("https://beautysync-stripeserver.onrender.com/transfer")
                        .addHeader("Content-Type", "application/json; charset=utf-8")
                        .post(body)
                        .build()

                    httpClient.newCall(request)
                        .enqueue(object : Callback {
                            override fun onFailure(call: Call, e: IOException) {
                            }

                            @SuppressLint("SetTextI18n")
                            override fun onResponse(call: Call, response: Response) {
                                when {
                                    !response.isSuccessful -> {

                                    }
                                    else -> {
                                        val responseData = response.body!!.string()
                                        val json =
                                            JSONObject(responseData)

                                        val transferId = json.getString("transferId")


                                        mHandler.post {
                                            val data1: Map<String, Any> = hashMapOf("transferId" to transferId, "orderId" to id, "date" to sdf.format(Date()), "amount" to "${itemPrice.toDouble() * 0.95 * 100}", "userImageId" to userImageId, "beauticianImageId" to beauticianImageId, "eventDate" to eventDate)
                                            db.collection("Transfers").document(orderId).set(data1)
                                        }
                                    }
                                }
                            }
                        })
                }
            }
        }
    }

}
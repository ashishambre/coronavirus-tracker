package com.ava.coronavirustracker.services;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.ava.coronavirustracker.models.LocationStats;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Service
public class CoronaVirusDataService {
	
	private static String VIRUS_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_19-covid-Confirmed.csv";
	private List<LocationStats> allStats = new ArrayList<>();
	private final OkHttpClient httpClient = new OkHttpClient();
	
	@PostConstruct
	@Scheduled(cron="* * 1 * * *")
	public void fetchVirusData() throws IOException {
		
		StringReader csvBodyReader;
		
		List<LocationStats> newStats = new ArrayList<>();
		
		Request request = new Request.Builder()
				.url(VIRUS_DATA_URL)
				.build();
		
		try(Response response = httpClient.newCall(request).execute()){
			if(!response.isSuccessful()) throw new IOException("Unexpected code " + response);
			
			csvBodyReader = new StringReader(response.body().string());
		}
		
		// CSV Reader
		Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);
		for (CSVRecord record : records) {
			LocationStats locationStat = new LocationStats();
			locationStat.setState(record.get("Province/State"));
			locationStat.setCountry(record.get("Country/Region"));
			int latestCases = Integer.parseInt(record.get(record.size() -1));
			int prevDayCases = Integer.parseInt(record.get(record.size() - 2));
			locationStat.setLatestTotalCases(latestCases);
			locationStat.setDiffFromPrevDay(latestCases - prevDayCases);

			newStats.add(locationStat);
		}
		
		this.allStats = newStats;
	}

	public List<LocationStats> getAllStats() {
		return allStats;
	}

}

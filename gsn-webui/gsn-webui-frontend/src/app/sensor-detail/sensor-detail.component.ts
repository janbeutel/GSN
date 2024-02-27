import { HttpClient } from '@angular/common/http';
import { Component, Inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FavoritesService } from '../services/favorites.service';
import { DownloadService } from '../services/download.service';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { AppComponent } from '../app.component';
import { LoginService } from '../services/login.service';
import { DOCUMENT } from '@angular/common';

@Component({
  selector: 'app-sensor-detail',
  templateUrl: './sensor-detail.component.html',
  styleUrls: ['./sensor-detail.component.scss']
})
export class SensorDetailComponent {
  loading: boolean = true;
  sensorName: string = '';
  today = new Date().toJSON();
  yesterday = new Date((new Date()).getTime() - (1000 * 60 * 60)).toJSON();

  date = {
    from: {
      date: this.yesterday.slice(0, 19),
      config: {
        dropdownSelector: '#dropdown2',
        minuteStep: 1,
      },
      onTimeSet: () => {
        if (new Date(this.date.from.date) > new Date(this.date.to.date)) {
          this.date.to.date = this.date.from.date;
        }
      },
    },
    to: {
      date: this.today.slice(0, 19),
      config: {
        dropdownSelector: '#dropdown2',
        minuteStep: 1,
      },
      onTimeSet: () => {
        if (new Date(this.date.from.date) > new Date(this.date.to.date)) {
          this.date.from.date = this.date.to.date;
        }
      },
    },
  };

  lineChartData: any[] = [];
  lineChartLabels: string[] = [];
  lineChartOptions: any = {
    responsive: true
  };
  lineChartLegend = true;

  formGroup = this.formBuilder.group({
    fromDate: [''], // Initial value for fromDate
    toDate: [''], // Initial value for toDate
  });
  pageSize: FormControl = new FormControl(25);
  dateFormGroup = this.formBuilder.group({
    startDate: new FormControl(new Date(new Date().getTime() - 1000 * 60 * 60)),
    endDate: new FormControl(new Date())
  }
  )

  truePageSize: number = 25;
  columns: boolean[] = [true, false, true];
  filterFunctionList: any[] = [];
  filterValuesList: any[][] = [[]]; // Initialize with a sample filter
  filterOperators: string[] = ['==', '!=', '>=', '>', '<=', '<'];
  details: any;
  sensorDetails: any;
  series: any;

  // Pagination properties
  pagedValues: any[] = [];
  currentPage: number = 1;
  constructor(
    @Inject(DOCUMENT) private document: Document,
    private http: HttpClient,
    private route: ActivatedRoute,
    private router: Router,
    private downloadService: DownloadService,
    private favoritesService: FavoritesService,
    private loginService: LoginService,
    private formBuilder: FormBuilder
  ) { }

  ngOnInit() {
    this.sensorName = this.route.snapshot.params['sensorname'];
    this.load();

  }

  updateRowCount() {
    this.truePageSize = Number(this.pageSize.value)
    this.updatePagedValues();
  }

  load() {
    const today = new Date().toJSON();
    const yesterday = new Date(new Date().getTime() - 1000 * 60 * 60).toJSON();

    this.date.from.date = yesterday.slice(0, 19);
    this.date.to.date = today.slice(0, 19);

    this.http.get(`http://walker.uibk.ac.at:8000/sensors/${this.sensorName}/${this.date.from.date}/${this.date.to.date}/`, { withCredentials: true }).subscribe(
      (data: any) => {
        this.loading = false;
        this.sensorDetails = data;
        this.details = data.properties ? data : undefined;
        console.log(this.details)
        this.buildData(this.details);
        this.updatePagedValues();
      },
      error => {
        console.log(error)
        // Handle error
      }
    );
  }

  submit() {
    const startDateControl = this.dateFormGroup.get('startDate');
    const startDate = startDateControl ? startDateControl.value : null;
    const endDateControl = this.dateFormGroup.get('endDate');
    const endDate = endDateControl ? endDateControl.value : null;

    if (startDate != null && endDate != null) {
      const from = new Date(startDate).toJSON();
      const to = new Date(endDate).toJSON();
      this.date.from.date = from.slice(0, 19);
      this.date.to.date = to.slice(0, 19);
      this.http.get(`http://walker.uibk.ac.at:8000/sensors/${this.sensorName}/${this.date.from.date}/${this.date.to.date}/`, { withCredentials: true }).subscribe(
        (data: any) => {
          this.loading = false;
          this.details = data.properties ? data : undefined;
          console.log(this.details)
          this.buildData(this.details);
          this.updatePagedValues();
        },
        error => {
          console.log(error)
          // Handle error
        }
      );
    } else {
      this.load()
    }

  }
  onFromDateChange() {
    if (new Date(this.date.from.date) > new Date(this.date.to.date)) {
      this.date.to.date = this.date.from.date;
    }
  }

  onToDateChange() {
    if (new Date(this.date.from.date) > new Date(this.date.to.date)) {
      this.date.to.date = this.date.from.date;
    }
  }

  buildData(details: any) {
    if (details != undefined && details.properties.values) {
      let offset = 0;

      this.lineChartData = [];
      this.lineChartLabels = [];
      const colors: { [key: string]: string } = {
        temperature: 'red',
        time: 'green',
        timestamp: 'blue',
        light: 'yellow',
        packet_type: 'grey'
        // Add more key-value pairs for other field names and colors
      };

      for (let k = 2; k < details.properties.fields.length; k++) {
        const dataset = {
          data: [] as number[],
          label: `${details.properties.fields[k].name} (${details.properties.fields[k].unit !== null ? details.properties.fields[k].unit : 'no unit'})`,
          backgroundColor: '',
          borderColor: ''
        };
        const fieldName = details.properties.fields[k].name;
        if (colors.hasOwnProperty(fieldName)) {
          dataset.backgroundColor = colors[fieldName];
          dataset.borderColor = colors[fieldName];
        } else {
          dataset.backgroundColor = 'rgba(0, 123, 255, 0.5)';
          dataset.borderColor = 'rgba(0, 123, 255, 1)';
        }

        console.log(details.properties.values.length)
        for (let i = 0; i < details.properties.values.length; i++) {
          if (typeof details.properties.values[i][k] === 'string' || details.properties.values[i][k] instanceof String) {
            offset++;
            break;
          }
          const dataPoint = details.properties.values[i][k];
          dataset.data.push(dataPoint);
        }

        if (dataset.data.length > 0) {
          this.lineChartData.push(dataset);
        }
      }
      for (let i = 0; i < details.properties.values.length; i++) {
        this.lineChartLabels.push(String(i))
      }
    }
  }




  download() {
    //this.downloadService.download(this);
  }

  addFavorite(sensorName: string) {
    this.favoritesService.add(sensorName).subscribe((resp) => {
      console.log(resp);
      this.load();
    }, (error: any) => {
      if (error.status == 302) {
        console.log(error)
        this.login();
      } else {
        console.error(error);
      }
    });
  }

  login(): void {
    this.loginService.getLoginUrl().subscribe((data: any) => {
      this.document.location.href = data.url;
    }, (error: any) => {
      console.error(error);
    });
  }

  removeFavorite(sensorName: string) {
    this.favoritesService.remove(sensorName).subscribe(() => {
      this.load();
    });
  }

  downloadCsv() {
    const sensorList = []
    sensorList.push(this.sensorName);
    const start = this.dateFormGroup.get('startDate');
    const end = this.dateFormGroup.get('endDate');
    let from;
    let to;
    if (start && start.value && start.value instanceof Date) {
      from = start && start.value ? start.value.toJSON() : '0';
    } else {
      from = start && start.value ? new Date(start.value).toJSON() : '0';
    }

    if (end && end.value && end.value instanceof Date) {
      to = end && end.value ? end.value.toJSON() : '0';
    } else {
      to = end && end.value ? new Date(end.value).toJSON() : '0';
    }


    if (from != '0' && to != '0') {
      this.downloadService.downloadMultiple(sensorList, from.slice(0, 19), to.slice(0, 19));
    } else {
      console.log("no dates")
    }

  }



  pageChanged(page: number) {
    this.currentPage = page;
    this.updatePagedValues();
  }

  updatePagedValues() {
    const startIndex = (this.currentPage - 1) * this.truePageSize;
    const endIndex = startIndex + this.truePageSize;
    this.pagedValues = this.details.properties.values.slice(startIndex, endIndex);
  }

  totalPages(): number {
    return Math.ceil(this.details.properties.values.length / this.truePageSize);
  }
  previousPage() {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.updatePagedValues();
    }
  }

  nextPage() {
    const totalPages = this.totalPages();
    if (this.currentPage < totalPages) {
      this.currentPage++;
      this.updatePagedValues();
    }
  }

  addFilter(ind: number, op: string, value: number, index: number) {
    this.filterValuesList.push([]);
    const filterFunc = (ind: number, op: string, value: number) => (a: any) => {
      try {
        return eval(a[ind] + op + value);
      } catch (e) {
        return false;
      }
    };

    this.filterFunctionList.splice(index, 1, filterFunc(ind, op, value));
  }

  filter() {
    let dataset = JSON.parse(JSON.stringify(this.details.properties.values));

    for (let j = 0; j < this.filterFunctionList.length; j++) {
      //console.log('Filter Criteria:', this.filterValuesList[j]);
      dataset = dataset.filter(this.filterFunctionList[j]);
    }

    const c = JSON.parse(JSON.stringify(this.details));

    c.properties.values = dataset;

    //console.log('Filtered Dataset:', dataset);

    this.buildData(c);
  }

  removeFilter(index: number) {
    this.filterFunctionList.splice(index, 1);
    this.filterValuesList.splice(index, 1);
    if (this.filterValuesList.length == 0) {
      this.filterValuesList = [[]]
      this.load()
    }
  }

  applyFilterChanges() {
    for (let i = 0; i < this.filterFunctionList.length; i++) {
      if (
        this.filterFunctionList[i] &&
        this.filterValuesList[i] &&
        this.filterValuesList[i][0] &&
        this.filterValuesList[i][1] &&
        this.filterValuesList[i][2]
      ) {
        this.addFilter(
          this.details.properties.fields.indexOf(this.filterValuesList[i][0]),
          this.filterValuesList[i][1],
          this.filterValuesList[i][2],
          i
        );
      }
    }
    this.filter()
  }

  compare() {
    /**
    localStorage.setItem(this.sensorName,JSON.stringify(this.lineChartData));
    const test = localStorage.getItem(this.sensorName);
    console.log(test)
    */
  }


}




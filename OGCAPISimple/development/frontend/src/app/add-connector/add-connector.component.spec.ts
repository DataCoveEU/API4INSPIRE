import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AddConnectorComponent } from './add-connector.component';

describe('AddConnectorComponent', () => {
  let component: AddConnectorComponent;
  let fixture: ComponentFixture<AddConnectorComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AddConnectorComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AddConnectorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

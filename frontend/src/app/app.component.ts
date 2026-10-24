import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ThemeService } from './core/theme.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  template: '<router-outlet />',
})
export class AppComponent {
  constructor(theme: ThemeService) {
    theme.init(); // aplica el tema guardado (claro/oscuro) al arrancar
  }
}

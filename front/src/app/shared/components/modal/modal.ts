import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './modal.html',
  styleUrls: ['./modal.css']
})
export class ModalComponent {
  @Input() title: string = 'Upload em Progresso';
  @Input() progress: number = 0;
  @Input() error: string | null = null;
  @Input() isVisible: boolean = false;
  
  @Output() close = new EventEmitter<void>();

  onClose() {
    this.close.emit();
  }
}

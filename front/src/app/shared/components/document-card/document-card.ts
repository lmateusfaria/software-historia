import { Component, Input, ChangeDetectionStrategy, EventEmitter, Output } from '@angular/core';
import { CommonModule, NgOptimizedImage, DatePipe } from '@angular/common';
import { DocumentoDTO } from '../../../core/models/documento.model';

@Component({
  selector: 'app-document-card',
  standalone: true,
  imports: [CommonModule, NgOptimizedImage, DatePipe],
  templateUrl: './document-card.html',
  styleUrls: ['./document-card.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DocumentCardComponent {
  @Input() document!: DocumentoDTO;
  @Input() index: number = 0;
  @Output() clickCard = new EventEmitter<number>();

  onCardClick() {
    if (this.document.id) {
      this.clickCard.emit(this.document.id);
    }
  }

  get delay() {
    return (this.index * 0.05) + 's';
  }
}

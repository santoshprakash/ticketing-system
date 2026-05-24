import { computed, Injectable, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { ModuleAccessResponse, ModuleCode } from '../models/admin.models';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class AccessService {
  private readonly modulesState = signal<ModuleCode[]>([]);
  private readonly superAdminState = signal(false);
  private readonly loadedState = signal(false);

  readonly modules = this.modulesState.asReadonly();
  readonly superAdmin = this.superAdminState.asReadonly();
  readonly loaded = this.loadedState.asReadonly();
  readonly hasAnyAccess = computed(() => this.superAdminState() || this.modulesState().length > 0);

  constructor(private readonly api: ApiService) {}

  load(): Observable<ModuleAccessResponse> {
    return this.api.post<ModuleAccessResponse>('/admin/access/me').pipe(
      tap((response) => {
        this.superAdminState.set(response.superAdmin);
        this.modulesState.set(response.modules);
        this.loadedState.set(true);
      })
    );
  }

  canAccess(moduleCode: ModuleCode): boolean {
    return this.superAdminState() || this.modulesState().includes(moduleCode);
  }

  clear(): void {
    this.superAdminState.set(false);
    this.modulesState.set([]);
    this.loadedState.set(false);
  }
}

import { Injectable } from '@nestjs/common';
import { CreateCaregiverDto } from './dto/create-caregiver.dto';
import { UpdateCaregiverDto } from './dto/update-caregiver.dto';

@Injectable()
export class CaregiverService {
  create(createCaregiverDto: CreateCaregiverDto) {
    return 'This action adds a new caregiver';
  }

  findAll() {
    return `This action returns all caregiver`;
  }

  findOne(id: number) {
    return `This action returns a #${id} caregiver`;
  }

  update(id: number, updateCaregiverDto: UpdateCaregiverDto) {
    return `This action updates a #${id} caregiver`;
  }

  remove(id: number) {
    return `This action removes a #${id} caregiver`;
  }
}

export class CreateUserDto {

        user_id: string
        email: string
        pass: string
        role: "patient" | "caregiver" | "admin" | "family"
        created_at: Date
        last_login: Date
}